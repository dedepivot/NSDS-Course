  /*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

#include "contiki.h"
#include "net/routing/routing.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define WITH_SERVER_REPLY  1
#define UDP_CLIENT_PORT	8765
#define UDP_SERVER_PORT	5678
#define MAX_CLIENT 10
#define PRINT_INTERVAL		  (60 * CLOCK_SECOND)
static struct simple_udp_connection udp_conn;
static uip_ipaddr_t allChildIP[MAX_CLIENT];
static uip_ipaddr_t parentIp[MAX_CLIENT];
static int counters[MAX_CLIENT];
static int childIPCount = 0;
static struct etimer periodic_timer;

PROCESS(udp_server_process, "UDP server");
AUTOSTART_PROCESSES(&udp_server_process);
/*---------------------------------------------------------------------------*/
static void
udp_rx_callback(struct simple_udp_connection *c,
         const uip_ipaddr_t *sender_addr,
         uint16_t sender_port,
         const uip_ipaddr_t *receiver_addr,
         uint16_t receiver_port,
         const uint8_t *data,
         uint16_t datalen)
{
  //LOG_INFO("Received\n");
  uip_ipaddr_t parent_ipaddr = *(uip_ipaddr_t *)data;

  int found = 0;
  int index = 0;
  for (int i = 0; i < childIPCount; i++) {
    if (uip_ipaddr_cmp(&allChildIP[i], sender_addr)) {
      found = 1;
      index = i;
      break;
    }
  }


  
  /*This check was implemented to prevent handling more nodes than MAX_CLIENT, 
  which could cause parent nodes to be omitted from the monitor due to the asynchronous nature of callbacks. 
  This issue was not addressed due to time constraints, as it was outside the main scope of the lab.

  if(!found && childIPCount==MAX_CLIENT){
    LOG_INFO_6ADDR(sender_addr);
    LOG_INFO(" is over the MAXCLIENT nodes\n");
    return; 
  }*/

  //adding of a new child to the tree
  if(!found){
    uip_ipaddr_copy(&allChildIP[childIPCount], sender_addr);
    uip_ipaddr_copy(&parentIp[childIPCount], &parent_ipaddr);
    index = childIPCount;
    childIPCount++;
    found = 0;
  }
  //update of parent IP of an already added child
  if(!uip_ipaddr_cmp(&parentIp[index], &parent_ipaddr)){
    uip_ipaddr_copy(&parentIp[index], &parent_ipaddr);
  }
  //msg received from index-child, reset its out-of-synch couter
  counters[index]=0;\
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_server_process, ev, data)
{

  PROCESS_BEGIN();
  LOG_INFO("UDP Server process started\n");
  /* Initialize client counter */
  for(int i=0; i<MAX_CLIENT; i++){
    counters[i]=0;
  }

  /* Initialize DAG root */
  NETSTACK_ROUTING.root_start();

  /* Initialize UDP connection */
  simple_udp_register(&udp_conn, UDP_SERVER_PORT, NULL,
                      UDP_CLIENT_PORT, udp_rx_callback);

  etimer_set(&periodic_timer, PRINT_INTERVAL);
  while(1){
    PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));
    for(int i = 0; i < childIPCount; i++){ 
      if(counters[i] <= -3){
        LOG_INFO_6ADDR(&allChildIP[i]);
        LOG_INFO(" Removed\n");
        uip_ipaddr_copy(&allChildIP[i], &allChildIP[childIPCount-1]);
        uip_ipaddr_copy(&parentIp[i], &parentIp[childIPCount-1]);
        counters[i]=counters[childIPCount-1];
        counters[childIPCount-1]=0;
        childIPCount--;
        i--;
      }else{
        LOG_INFO_6ADDR(&allChildIP[i]);
        LOG_INFO_(" is connected to ");
        LOG_INFO_6ADDR(&parentIp[i]);
        LOG_INFO_("\n");
      }
      counters[i]--;
    }
    /* reset time*/
    etimer_set(&periodic_timer, PRINT_INTERVAL);
  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/

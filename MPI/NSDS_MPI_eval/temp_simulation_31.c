#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>

// Group number: 31
// Group members:
// - Bossi Nicolò  | 10710040
// - Grisoni Samuele | 10810440
// - Oliva Antonio  | 10819753

const double L = 100.0;                 // Length of the 1d domain
const int n = 1000;                     // Total number of points
const int iterations_per_round = 1000;  // Number of iterations for each round of simulation
const double allowed_diff = 0.001;      // Stopping condition: maximum allowed difference between values

double initial_condition(double x, double L) {
    return fabs(x - L / 2);
}

int main(int argc, char **argv) {
    MPI_Init(&argc, &argv);

    int rank, size;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    int tempsPerProcess = n/size;
    double *myTemps = malloc(sizeof(double)*(tempsPerProcess));

    //Each process calculate the starting temp of their managed points
    for(int i = 0; i < tempsPerProcess; i++){
        double pointPosition = (i+rank*tempsPerProcess)*(L/n);
        myTemps[i] = initial_condition(pointPosition, L);
    }
    
    //Our first thought was to implent inizialization using process 0 as a centralized solution.
    //To reduce comunication overhead and synchronization we develop the solution above
    /*double *globalTemp = NULL;
    if(rank==0){
        globalTemp = malloc(sizeof(double)*n);
        for(int i = 0; i < n; i++){
            double pointPosition = i*(L/n);
            globalTemp[i] = initial_condition(pointPosition, L);
            printf("%f ", globalTemp[i]);
        }
        printf("\n");
    }
    MPI_Scatter(globalTemp, tempsPerProcess, MPI_DOUBLE, myTemps, tempsPerProcess, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    if(rank==0){
        free(globalTemp);
    }*/


    double *newTemps;
    int round = 0;
    while (1) {
        // Perform one round of iterations
        round++;
        for (int t = 0; t < iterations_per_round; t++) {
            double lastPrevTemp;
            double firstSuccTemp;
            newTemps = malloc(sizeof(double)*tempsPerProcess);
            //I'm not the last process, sending my last managed temp and receive fist manage temp of my succ
            if(rank!=size-1){
                MPI_Sendrecv(&myTemps[tempsPerProcess-1], 1, MPI_DOUBLE, rank+1, 0, &firstSuccTemp, 1, MPI_DOUBLE, rank+1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            }
            //I'm  not the first process, sending my first temp and waiting for the last temp of my predecessor 
            if(rank!=0){
                MPI_Sendrecv(&myTemps[0], 1, MPI_DOUBLE, rank-1, 0, &lastPrevTemp, 1, MPI_DOUBLE, rank-1, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            }
            //printf("I'm %d, firstTemp %f, lastTemp %f\n", rank, firstSuccTemp, lastPrevTemp);

            //Cover edge case (first or last temp) of each subarray of each process; else is the normal case
            for(int i = 0; i < tempsPerProcess; i++){
                if(i==0){
                    if(rank==0){
                        newTemps[i] = (myTemps[i] + myTemps[i+1])/2;
                    }else{
                        newTemps[i] = (lastPrevTemp + myTemps[i] + myTemps[i+1])/3;
                    }
                }else if(i==tempsPerProcess-1){
                    if(rank == size - 1){
                        newTemps[i] = (myTemps[i] + myTemps[i-1])/2;
                    }else{
                        newTemps[i] = (myTemps[i-1] + myTemps[i] + firstSuccTemp)/3;
                    }
                }else{
                    newTemps[i] = (myTemps[i-1] + myTemps[i] + myTemps[i+1])/3;
                }
            }
            free(myTemps);
            myTemps = newTemps;
            /*printf("I'm rank %d\n", rank);
            for(int i = 0; i < tempsPerProcess; i++){
                printf("%f ", myTemps[i]);
            }
            printf("\n");*/
        }
        
        double global_min, global_max, max_diff;
        double myMin, myMax;
        myMin = myTemps[0];
        myMax = myMin;
        for(int i = 0; i<tempsPerProcess; i++){
            if(myTemps[i]<myMin){
                myMin = myTemps[i];
            }
            if(myTemps[i]>myMax){
                myMax = myTemps[i];
            }
        }
        MPI_Allreduce(&myMin, &global_min, 1, MPI_DOUBLE, MPI_MIN, MPI_COMM_WORLD);
        MPI_Allreduce(&myMax, &global_max, 1, MPI_DOUBLE, MPI_MAX, MPI_COMM_WORLD);
        max_diff = global_max-global_min;
        if (rank == 0) {
            printf("Round: %d\tMin: %.5f\tMax: %.5f\tDiff: %.5 f\n", round, global_min, global_max, max_diff);
        }
        if(max_diff < allowed_diff){
            break;
        }
    }

    free(myTemps);

    MPI_Finalize();
    return 0;
}

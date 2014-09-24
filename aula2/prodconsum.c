#include <pthread.h>
#include <stdlib.h> /* required for rand_r(...) */ 
#include <stdio.h>
#include "buffer.h"
#include <time.h>

int sleepTime, producersN, consumersN;

pthread_mutex_t mVar;

int main(int argc, char*argv[]) {

if (argc != 4) {
    printf("Usage: SLEEPTIME PRODUCERSN CONSUMERSN\n");
    exit(0);
}

sleepTime = atoi(argv[1]);
producersN = atoi(argv[2]);
consumersN = atoi(argv[3]);

/* 2 Initialize buffer, mutex, semaphores, and other global vars */
/* 3 Create producer thread(s) */
/* 4 Create consumer thread(s) */
/* 5 Sleep */
/* 6 Release resources, e.g. destroy mutex and semaphores */
/* 7 Exit */ 

}

void *producer(void *param) {
	buffer_item rand;
	while (1) {
	pthread_mutex_lock(&mVar);
	/* sleep for a random period of time */
	sleep(sleepTime);
	/* generate a random number */
	unsigned int seed = time(NULL);
	rand = rand_r(&seed);
	if (insert_item(rand) < 0)
		printf("potato2"); // report error condition
	}
}

void *consumer(void *param) { 
	buffer_item rand;
	while (1) {
		pthread_mutex_lock(&mVar);
		/* sleep for a random period of time */
		sleep (sleepTime);
		if (remove_item(&rand) < 0)
			printf("potato"); // report error condition
		pthread_mutex_unlock(&mVar); 
	}
}

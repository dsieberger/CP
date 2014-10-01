#include <pthread.h>
#include <stdlib.h> /* required for rand_r(...) */ 
#include <stdio.h>
#include "buffer.h"
#include <semaphore.h>

#define RANDLIMIT 10000000

int sleepTime, producersN, consumersN;

pthread_mutex_t mVar;
pthread_attr_t attr;
pthread_t id;

buffer_item buffer[BUFFER_SIZE];

sem_t used;
sem_t free;

int counter;
int seed;

int main(int argc, char*argv[]) {

/*Inicialização*/
sem_init (&used, 0, 0);
seed = 69;
sem_init (&free, 0, BUFFER_SIZE);
counter = 0;
pthread_attr_init(&attr);


if (argc != 4) {
    printf("Usage: SLEEPTIME #PRODUCERS #CONSUMERS\n");
    exit(0);
}

sleepTime = atoi(argv[1]);
producersN = atoi(argv[2]);
consumersN = atoi(argv[3]);


for (int i = 0; i < producersN; i++ ){
		pthread_create(&id, &attr, &producer, NULL);
}

for (int i = 0; i < consumersN; i++ ){
		pthread_create(&id, &attr, &consumer, NULL);
	} 
sleep(sleepTime);
exit(0);

}

void *producer(void *param) {
	buffer_item rand_item;

	while (1) {

	int randT = rand_r(&seed);
	/* sleep for a random period of time */
	sleep(randT);
	/* generate a random number */
	int item = rand();
	
	sem_wait(&free);
	pthread_mutex_lock(&mVar);	
	
	if (insert_item(item) < 0)
		printf("Cannot add to buffer"); // report error condition
	}

	pthread_mutex_unlock(&mVar);
	sem_post(&used);
}

void *consumer(void *param) { 
	buffer_item rand_item;
	
	while (1) {
		
		/* sleep for a random period of time */
		int randT = rand_r(&seed);
		/* sleep for a random period of time */
		sleep(randT);

		sem_wait (&used);
		pthread_mutex_lock(&mVar);

		if (remove_item(&rand_item) < 0)
			printf("Cannot consume from buffer"); // report error condition
		else printf("consumer consumed %d\n", item);
		
		pthread_mutex_unlock(&mVar); 
		sem_post(&free);
	}
}

int insert_item(buffer_item item) {
   if(counter < BUFFER_SIZE) {
      buffer[counter] = item;
      counter++;
      return 0;
   }
   else { 
      return -1;
   }
}

int remove_item(buffer_item *item) {
   if(counter > 0) {
      * item = buffer[(counter-1)];
      counter--;
      return 0;
   }
   else {
      return -1;
   }
}

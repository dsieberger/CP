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
sem_t empty;

int counter;

void *producer(void *param);
void *consumer(void *param);

int main(int argc, char*argv[]) {

/*Inicialização*/
sem_init (&used, 0, 0);

sem_init (&empty, 0, BUFFER_SIZE);
counter = 0;
pthread_attr_init(&attr);


if (argc != 4) {
    printf("Usage: SLEEPTIME #PRODUCERS #CONSUMERS\n");
    exit(0);
}

sleepTime = atoi(argv[1]);
producersN = atoi(argv[2]);
consumersN = atoi(argv[3]);

int i = 0;
while ( i < producersN){
		pthread_create(&id, &attr, producer, NULL);
		i++;
}

i = 0;
while ( i < consumersN){
		pthread_create(&id, &attr, consumer, NULL);
		i++;
	} 

sleep(sleepTime);
exit(0);

}

void *producer(void *param) {
	buffer_item rand_item;

	while (1) {
	unsigned int seed = time(NULL);
	int randT = rand_r(&seed) % 10;
	/* sleep for a random period of time */
	sleep(randT);
	/* generate a random number */
	int item = rand() % 10;
	
	sem_wait(&empty);
	pthread_mutex_lock(&mVar);	
	
	if (insert_item(item) < 0)
		printf("Cannot add to buffer\n"); // report error condition
	else printf("Produced %d\n", item);
	} 

	pthread_mutex_unlock(&mVar);
	sem_post(&used);
}

void *consumer(void *param) { 
	buffer_item rand_item;
	
	while (1) {
		unsigned int seed = time(NULL);
		/* sleep for a random period of time */
		int randT = rand_r(&seed) % 10;
		/* sleep for a random period of time */
		sleep(randT);

		sem_wait (&used);
		pthread_mutex_lock(&mVar);

		if (remove_item(&rand_item) < 0)
			printf("Cannot consume from buffer"); // report error condition
		else printf("consumer consumed %d\n", rand_item);
		
		pthread_mutex_unlock(&mVar); 
		sem_post(&empty);
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

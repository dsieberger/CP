
#include "buffer.h"

buffer_item buffer [BUFFER_SIZE]

int insert_item(buffer_item item){
	printf ("producer produced %d\n", item);
}

int remove_item(buffer_item *item){
	printf("consumer consumed %d\n", item.number);
}
CC=gcc
CFLAGS=-I. -lpthread -lm
DEPS= ncoils.h ncoilsServer.h
OBJ = ncoilsd.o ncoils.o logging.o sockets.o

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

ncoils: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)
	
clean: 
	rm -fr *.o
	rm -fr ncoils

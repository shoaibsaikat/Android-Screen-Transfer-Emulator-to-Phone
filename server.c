#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>

#define BUFFUER_LEN 1024

void error(const char *msg) {
	perror(msg);
    exit(1);
}

int serverSocket, clientSocket = -1, clientSocket1 = -1, clientSocket2 = -1, serverPort;
socklen_t clientLen;
int bRun = 1;
int bufferSize = 1024;
int bufferNum = 0;
int timeOut = 50 * 1000;

char buffer1[BUFFUER_LEN], buffer2[BUFFUER_LEN];
struct sockaddr_in serverAddress, clientAddress;
int n;

void* connect1(void *ptr) {

	while(read(clientSocket1, buffer1, bufferSize) && bRun) {
		usleep(timeOut);
    	write(clientSocket2, buffer1, bufferSize);

		if(strncmp(buffer1, "quit", 4) == 0) bRun = 0;

		bzero((char *) &buffer1, BUFFUER_LEN);
	} 
}

void* connect2(void *ptr) {

	while(read(clientSocket2, buffer2, bufferSize) && bRun) {
		usleep(timeOut);
    	write(clientSocket1, buffer2, bufferSize);

		if(strncmp(buffer2, "quit", 4) == 0) bRun = 0;

		bzero((char *) &buffer2, BUFFUER_LEN);
	} 
}

int main(int argc, char *argv[]) {

	int numOfClient = 0;
	if (argc < 2) {
        fprintf(stderr,"ERROR, no port provided\n");
        exit(1);
    }

    serverPort = atoi(argv[1]);

	pthread_t thread1, thread2;

	serverSocket = socket(AF_INET, SOCK_STREAM, 0);

	if (serverSocket < 0) 
		error("ERROR opening serverSocket");

	if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &(int){ 1 }, sizeof(int)) < 0)
    	error("setsockopt(SO_REUSEADDR) failed");

	setsockopt(serverSocket, SOL_SOCKET, SO_SNDBUF, (char *)&bufferSize, sizeof(bufferSize));

    bzero((char *) &serverAddress, sizeof(serverAddress));

    serverAddress.sin_family = AF_INET;
    serverAddress.sin_addr.s_addr = INADDR_ANY;
    serverAddress.sin_port = htons(serverPort);
    if (bind(serverSocket, (struct sockaddr *) &serverAddress, sizeof(serverAddress)) < 0)
		error("ERROR on binding serverAddress");

	listen(serverSocket, 5);
	clientLen = sizeof(clientAddress);

	while((clientSocket = accept(serverSocket, (struct sockaddr *) &clientAddress, &clientLen)) && clientSocket > 0)
	{
		numOfClient++;
		if(numOfClient%2) clientSocket1 = clientSocket;
		else clientSocket2 = clientSocket;

		if(numOfClient == 2) break;
	}

    pthread_create(&thread1, NULL, connect1, NULL);
    pthread_create(&thread2, NULL, connect2, NULL);
    
	pthread_join(thread1, NULL);
	pthread_join(thread2, NULL);

	close(clientSocket1);
	close(clientSocket2);
    close(serverSocket);

    exit(EXIT_SUCCESS);
}

a. Project Description
This project is about understanding multithreading with shared state and serialization. This involves designing a task-based game that runs on the host. The game can be 
played in the single player as well as the multiplayer mode. If multiple players join the game simultaneously, they share the same game and game state. Hence, it can be 
played as a team where each player logs into the game from their terminal. The game is based on tasks where each task is a simple question, which if answered correctly, 
will reveal an image on the screen partially. If the player or the group of players playing together as a team win the game by answering all questions correctly, the image is completely revealed. The project involves data serialization using protobuf that encodes and decodes the bytes sent or receive across the wire based on a pre-set request and response protocol.

b. Running Program
The default values for host and port and threads are given below.
host=localhost
port=9099

gradle runClient -Phost=<hostname> -Pport=<port> OR
gradle runClient

gradle runServer  -Pport=<port> OR
gradle runServer


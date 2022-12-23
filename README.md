Links to ScreenCast: https://youtu.be/_AfqEjaFx68

a. Project Description
This project is about understanding multithreading with shared state and serialization. This involves designing a task-based game that runs on the host. The game can be 
played in the single player as well as the multiplayer mode. If multiple players join the game simultaneously, they share the same game and game state. Hence, it can be 
played as a team where each player logs into the game from their terminal. The game is based on tasks where each task is a simple question, which if answered correctly, 
will reveal an image on the screen partially. If the player or the group of players playing together as a team win the game by answering all questions correctly, the image 
is completely revealed. The project involves data serialization using protobuf that encodes and decodes the bytes sent or receive across the wire based on a pre-set request 
and response protocol.

b. Requirements Checklist and Implementation
1. Yes
2. Yes
3. Yes
4. Yes
5. Yes
6. Yes
7. Yes
8. Yes
9. Yes
10. Yes
11. Yes
12. Yes
13. Yes
14. Yes
15. Yes – The game has been gamified based on the number wins of a player. If a player wins a game, the next time he/she logs in, the image will be revealed one task earlier 
than the last successful attempt.
16. Posted in #servers channel (gradle runClient -Phost=54.196.235.43 -Pport=9099 -q --console=plain)
17. No
18. Yes – The player can exit while playing the game by giving command “exit”

c. Running Program
The default values for host and port and threads are given below.
host=localhost
port=9099

gradle runClient -Phost=<hostname> -Pport=<port> OR
gradle runClient

gradle runServer  -Pport=<port> OR
gradle runServer


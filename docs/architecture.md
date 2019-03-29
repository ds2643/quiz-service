# Design Architecture
The quiz service implements a minimal architecture with two main components: An HTTP server and relational database.

The current implementation is designed to be simple enough to accommodate to criticism and unforeseen demands.

This document will describe the current implementation and potential directions for improvement.

## Current Implementation
The server provides a means of communication between this backend quiz service and other applications.

The current server api (see docs/api.md for more details) implements a collection of mechanisms corresponding to users' behavior.

### Sequence of User Behavior
1. The basic information provided to the user is communicated to the quiz service via the `/create-session` route, which assigns and echos a session id (e.g., `"GHjYZ-efGdX-S5orz"`). When the session is created, a set of required responses is added to a global response table in the database.
2. Using this session id, the caller may query for a set of required responses associated with the active session using `/answer-ids`.
3. For each question included in the list returned by the request to `/answer-ids`, the caller my request the associated question content using `/get-question`.
4. Once the caller is ready to submit a response associated with an answer, the integer value of this answer (representing an enumerated choice) may be sent to the quiz server with `/submit-answer`.
5. Crucially, since questions and responses are requested and sent to the server independently, the calling application should be able to persist the state of a session without needing to maintain a single browser window instance.
6. Finally, when the user finishes answering questions, the session will end following a request to `/confirm-submission`.

### Persistence
The included MySQL database persists data associated with quiz sessions. Communication between the quiz server and this database occurs over the network through HTTP. Latency in communication associated with this mechanism should not present issues as the robustness of the service requires only modest speeds for reads and writes to the database.

The choice of a relational database over NoSQL alternatives reflects a choice to refrain from premature optimization in any particular dimension in favor of a well-understood solution. SQL offers the advantage of accessibly to people of many backgrounds since the technology is ubiquitous across many industries. MySQL offers a mature, well-documented, and commonly-used implementation of a relational DBMS.

The packaged Dockerized MySQL database represents a test rather than production-grade solution to persistence. However, the implementation may be reconfigured to use a more permanent solution. One attractive choice possibly worth exploring is AWS RDS, which offers a cloud-based MySQL solution. Such an options several advantages in production, such as managing backups and scaling.

### Database Schema
The database is broken into four tables serving the needs of session logic defined in the `quiz-service.sessions` module.

1. Sessions: Stores user information associated with sessions.
2. Answers: Stores user answers to questions.
3. Questions: Stores content associated with questions.
4. Rules: Contains rules constraining user behavior during quiz. For now, only includes max time allocated.

An improved implementation would also address the need to store assets. MySQL might provide a nice mechanism for storing versioned references to such data.

### Possible Deployment Solutions
In the context of a production environment, we should aim to optimize against the risk of single points of failure. A good starting point might be containerizing of this application to better serve Kupernetes. For instance, we should consider delegating requests from the caller amongst several quiz servers: If a single server fails, the system as a whole may continue healthily.

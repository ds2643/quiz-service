# API
The quiz service implements the following routes:

## POST `/create-session`
This route allows the creation of a new session associated with a user.

In the body of the the request, specify user identification details:

``` json
{
  "user-config": {
    "firstname": "John",
    "lastname": "Smith",
    "email": "john@smith.org"
  }
}
```
The `"body"` section of the response message includes an entry for `"session-id"`.

## GET `/answer-ids`
This route finds the set of answers a test-taker is responsible for submitting.

In the body of the the request, specify the session id:

``` json
{
  "session-id": "ZAUQa-BiK04-EC0Go"
}
```
The `"body"` section of the response message includes an entry for `"answers"`, which indicate the list of answer ids the test-taker must submit.

## GET `/get-question`
This route retrieves the content of the question associated with an answer id.

``` json
{
  "answer-id": "gB6gl-GlbCZ-x5KKA"
}
```

The response includes a content field in the body.

## PUT `/submit-answer`
This route allows for submission of answers:

``` json
{
  "submission": {
    "response": 3,
    "session-id": "some-session-id",
    "id": "some-question-id"
  }
}
```

Please note that responses are constrained to integers. These integers are intended to communicate answer enumerations.

Please also note that answers are not allowed after the allocated time has passed.

The response includes a status field confirming the result of the submission.

## POST `confirm-submission`
Once all responses have been submitted, this route may be used to terminate the quiz session.

``` json
{
  "session-id": "some-session-id"
}
```

The status field of the response returns "ok" if the confirmation is successful.

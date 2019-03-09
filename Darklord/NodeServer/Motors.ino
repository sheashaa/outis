
void LeftDirection(int direction) {
  switch (direction) {
    case FORWARD:
      digitalWrite(MOTOR_LEFT_DIR1, HIGH);
      digitalWrite(MOTOR_LEFT_DIR2, LOW);
      break;
    case REVERSE:
      digitalWrite(MOTOR_LEFT_DIR1, LOW);
      digitalWrite(MOTOR_LEFT_DIR2, HIGH);
      break;
    case STOP:
      digitalWrite(MOTOR_LEFT_DIR1, LOW);
      digitalWrite(MOTOR_LEFT_DIR2, LOW);
      break;
  }
}

void RightDirection(int direction) {
  switch (direction) {
    case FORWARD:
      digitalWrite(MOTOR_RIGHT_DIR1, HIGH);
      digitalWrite(MOTOR_RIGHT_DIR2, LOW);
      break;
    case REVERSE:
      digitalWrite(MOTOR_RIGHT_DIR1, LOW);
      digitalWrite(MOTOR_RIGHT_DIR2, HIGH);
      break;
    case STOP:
      digitalWrite(MOTOR_RIGHT_DIR1, LOW);
      digitalWrite(MOTOR_RIGHT_DIR2, LOW);
      break;
  }
}

void Forward() {
  LeftDirection(FORWARD); RightDirection(FORWARD);
}

void Backward() {
  LeftDirection(REVERSE); RightDirection(REVERSE);
}

void Left() {
  LeftDirection(REVERSE); RightDirection(FORWARD);
}

void Right() {
  LeftDirection(FORWARD); RightDirection(REVERSE);
}

void Stop() {
  LeftDirection(STOP); RightDirection(STOP);
}

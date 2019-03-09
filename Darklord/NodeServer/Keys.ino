
//   Y
// X   B
//   A

void ProcessKeys(char key, int val) {
  yield();
  switch (key) {
    case 'a': if (IsRunning) HandleKeyA(val); break;
    case 'p': if (IsRunning) HandleKeyB(val); break;
    case 'o': if (IsRunning) HandleKeyY(val); break;
    case 'i': if (IsRunning) HandleKeyX(val); break;
    case 'd': if (IsRunning) HandleDPad(val); break;
    case 'w': case 'e': case 'q': case 'r': if (IsRunning) HandleTriggers(key, val); break;
    case 'u': HandleStartKey(val); break;
  }
  yield();
}

void HandleKeyA(int val) {
  switch (val) {
    case 1:
      Serial.println("A on");
      FrontSpinner(ON);
      break;
    case 0:
      Serial.println("A off");
      FrontSpinner(OFF);
      break;
  }
}

void HandleKeyB(int val) {
  switch (val) {
    case 1:
      Serial.println("B on");
      GroundSpinner(ON);
      break;
    case 0:
      Serial.println("B off");
      GroundSpinner(OFF);
      break;
  }
}

void HandleKeyX(int val) {
  switch (val) {
    case 1:
      Serial.println("X on");
      FrontSpinner(ON);
      break;
    case 0:
      Serial.println("X off");
      FrontSpinner(OFF);
      break;
  }
}

void HandleKeyY(int val) {
  switch (val) {
    case 1:
      Serial.println("Y on");
      GroundSpinner(ON);
      break;
    case 0:
      Serial.println("Y off");
      GroundSpinner(OFF);
      break;
  }
}

void HandleTriggers(char key, int val) {
  switch (key) {
    // Top Left Trigger
    case 'w':
      switch (val) {
        case 1:
          Serial.println("Top left trigger on");
          break;
        case 0:
          Serial.println("Top left trigger off");
          break;
      }
      break;

    // Top Right Trigger
    case 'e':
      switch (val) {
        case 1:
          Serial.println("Top right trigger on");
          break;
        case 0:
          Serial.println("Top right trigger off");
          break;
      }
      break;

    // Bottom Left Trigger
    case 'q':
      switch (val) {
        case 255:
          Serial.println("Bottom left trigger on");
          break;
        case 0:
          Serial.println("Bottom left trigger off");
          break;
      }
      break;

    // Bottom Right Trigger
    case 'r':
      switch (val) {
        case 255:
          Serial.println("Bottom right trigger on");
          break;
        case 0:
          Serial.println("Bottom right trigger off");
          break;
      }
      break;
  }
}

void HandleDPad(int val) {
  switch (val) {
    //Dpad Center Button
    case 0:
      Serial.println("D-pad Center");
      Stop();
      break;
    //Dpad Left Button
    case 1:
      Serial.println("D-pad Left");
      Left();
      break;
    //Dpad Up Button
    case 2:
      Serial.println("D-pad Up");
      Forward();
      break;
    //Dpad Right Button
    case 3:
      Serial.println("D-pad Right");
      Right();
      break;
    //Dpad Down Button
    case 4:
      Serial.println("D-pad Down");
      Backward();
      break;
  }
}

void HandleStartKey(int val) {
  switch (val) {
    case 1:
      Serial.println("Start on");
      IsRunning = true;
      break;
    case 0:
      Serial.println("Start off");
      break;
  }
}

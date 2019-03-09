
void GroundSpinner(int state) {
  switch (state) {
    case ON: digitalWrite(GROUND_SPINNER, HIGH); break;
    case OFF: digitalWrite(GROUND_SPINNER, LOW); break;
  }
}

void FrontSpinner(int state) {
  switch (state) {
    case ON: digitalWrite(FRONT_SPINNER, HIGH); break;
    case OFF: digitalWrite(FRONT_SPINNER, LOW); break;
  }
}

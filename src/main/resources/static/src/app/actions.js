/* PetCARE — Running a server action from a button
   ============================================================
   runAction(call, onSuccess):
     1. ignores a second click while one action is already running (no double submissions)
     2. calls the API
     3. on failure shows the server's message in a toast and leaves the screen as it was
     4. on success reloads the signed-in user's data from the server, so every screen shows what the
        database now says, then runs onSuccess(responseData).
   Returns true when the action succeeded. */
async function runAction(call, onSuccess){
  if(UI._busy) return false;
  UI._busy = true;
  try{
    const r = await call();
    if(!r.ok){
      if(r.status !== 401) showToast(r.error);       // a 401 already sent the user back to the sign-in screen
      return false;
    }
    UI._previews = {};                                // eligibility previews may be out of date now
    const h = await hydrate();
    if(!h.ok) return false;
    if(onSuccess) onSuccess(r.data);
    return true;
  } finally {
    UI._busy = false;
  }
}

/* Called by Api when the server says the session is no longer valid (expired, signed out elsewhere, account deactivated). */
function onSessionLost(){
  if(!AuthService.getCurrentUser()) return;           // already on the sign-in screen
  clearLocalData();
  UI = defaultUI();
  render();
  showToast("Your session has ended. Please sign in again.");
}

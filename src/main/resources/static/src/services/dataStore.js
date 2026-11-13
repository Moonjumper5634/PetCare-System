/* PetCARE — In-memory copy of the signed-in user's data
   ============================================================
   S has the same shape the screens have always read (users, pets, registrations, requests, adoptions,
   shelterPets, notifications, audit, criteria). It is no longer stored in the browser: hydrate() fills it from
   GET /api/bootstrap after sign-in and after every successful action, and the server decides what each role
   may see (a resident receives only their own records; no password data is ever sent).
   Nothing here writes data. All changes go through the API (see runAction). */

function blankState(){
  return { users:[], pets:[], registrations:[], requests:[], adoptions:[], shelterPets:[], notifications:[], audit:[], criteria:{} };
}
var S = blankState();
var CURRENT_USER = null;

function parseISODate(s){
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(s || "");
  return m ? new Date(+m[1], +m[2] - 1, +m[3]) : TODAY;
}

/* Reload everything the signed-in user may see. Also adopts the server's idea of "today". */
async function hydrate(){
  const r = await Api.get("/api/bootstrap");
  if(!r.ok) return r;
  S = r.data.state;
  CURRENT_USER = r.data.user;
  if(CURRENT_USER.role === "admin"){
    const sr = await Api.get("/api/admin/settings");
    S.settings = sr.ok ? sr.data : {};
  }
  TODAY = parseISODate(r.data.today);
  return r;
}
function clearLocalData(){ S = blankState(); CURRENT_USER = null; }


/* ---------------- LOOKUP HELPERS ----------------
   Thin wrappers over the service layer so screens never touch raw data directly. */
function currentUser(){ return AuthService.getCurrentUser(); }
function userById(id){ return ResidentService.getUserById(id); }
function petById(id){ return PetService.getPetById(id); }
function regById(id){ return VeterinaryService.getRegistrationById(id); }
function reqById(id){ return VeterinaryService.getRequestById(id); }
function adoptById(id){ return VeterinaryService.getAdoptionById(id); }
function shelterById(id){ return VeterinaryService.getShelterPetById(id); }
function ownerPets(ownerId){ return PetService.getPetsByOwner(ownerId); }
function verifiedOwnerPets(ownerId){ return PetService.getVerifiedPetsByOwner(ownerId); }

function unreadCount(userId){
  return S.notifications.filter(n=>n.userId===userId && !n.read).length;
}

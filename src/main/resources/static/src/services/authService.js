/* PetCARE — Authentication service (talks to the server)
   ============================================================
   The server owns the session (an HttpOnly cookie), the password check, lockout and one-time codes.
   The browser only remembers who is signed in for the current page (CURRENT_USER, filled by hydrate()).
   Roles used internally: "owner" (Resident), "staff" (CVO Staff), "admin" (Administrator). */
const AuthService = (function(){

  async function login(email, password){
    const r = await Api.post("/api/auth/login", { email:String(email || "").trim(), password:String(password || "") });
    if(!r.ok) return { ok:false, error:r.error };
    const h = await hydrate();
    if(!h.ok) return { ok:false, error:h.error };
    return { ok:true, user:CURRENT_USER };
  }

  /* Page load: is there still a valid session cookie? If so, load the data and carry on signed in. */
  async function restore(){
    const h = await hydrate();
    if(!h.ok) clearLocalData();
    return h.ok;
  }

  async function logout(){
    await Api.post("/api/auth/logout");
    clearLocalData();
  }

  function getCurrentUser(){
    if(!CURRENT_USER) return null;
    return S.users.find(x => x.id === CURRENT_USER.id) || CURRENT_USER;
  }
  function isAuthenticated(){ return !!CURRENT_USER; }
  function getUserRole(){ const u = getCurrentUser(); return u ? u.role : null; }
  function hasRole(...roles){ return roles.includes(getUserRole()); }

  async function changePassword(currentPassword, newPassword){
    const r = await Api.post("/api/auth/change-password", { currentPassword, newPassword });
    if(!r.ok) return { ok:false, error:r.error };
    await hydrate();                      // clears the "temporary password" flag
    return { ok:true };
  }

  async function updateProfile(profile){
    const r = await Api.put("/api/auth/profile", profile);
    if(!r.ok) return {ok:false,error:r.error};
    await hydrate();
    return {ok:true};
  }

  return { login, restore, logout, getCurrentUser, isAuthenticated, getUserRole, hasRole, changePassword, updateProfile };
})();

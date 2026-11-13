/* PetCARE — Login, logout, sign-up, verification and forgot-password flows (UI layer over the server)
   The server checks passwords, issues and checks one-time codes, and decides everything; these functions only
   collect input, show the server's answer and move between screens. */

/* ---------------- LOGIN / LOGOUT ---------------- */
async function doLogin(email, password){
  if(UI._busy) return;
  UI._busy = true;
  try{
    UI.errors = {};
    UI.loginEmail = String(email || "").trim();
    const result = await AuthService.login(email, password);
    if(!result.ok){ UI.errors.login = result.error; render(); return; }
    UI.loginEmail = "";
    UI.screen = result.user.mustChangePassword ? "profile" : AuthGuard.homeScreen(result.user.role);
    render();
    showToast(result.user.mustChangePassword
      ? "Please change your temporary password to continue."
      : `Welcome back, ${result.user.name}.`);
  } finally { UI._busy = false; }
}
async function doLogout(){
  await AuthService.logout();
  UI = defaultUI();   // clears wizards, selections, modals and drafts
  render();
}

/* ---------------- SIGN-UP / VERIFY / FORGOT ---------------- */

function startSignup(){
  UI.signupDraft = {firstName:"", middleName:"", lastName:"", email:"", mobile:"", houseStreet:"", barangay:BARANGAYS[0], city:"Santa Rosa", province:"Laguna", password:"", confirm:"", agree:false};
  UI.authScreen = "signup";
  render();
}
async function submitSignup(data){
  const errs = {};
  if(!data.firstName) errs.firstName = "Required field";
  if(!data.lastName) errs.lastName = "Required field";
  if(!data.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errs.email = "Invalid email";
  if(!data.mobile) errs.mobile = "Required field";
  if(!data.barangay) errs.barangay = "Missing barangay";
  if(!data.password || data.password.length < 8) errs.password = "Password must be at least 8 characters";
  else if(!/[A-Za-z]/.test(data.password) || !/[0-9]/.test(data.password)) errs.password = "Password must contain a letter and a number";
  if(data.password !== data.confirm) errs.confirm = "Password mismatch";
  if(!data.agree) errs.agree = "Required field";
  if(Object.keys(errs).length){ UI.errors = errs; UI.signupDraft = data; render(); return; }
  if(UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/signup", {
      firstName:data.firstName, middleName:data.middleName, lastName:data.lastName, email:data.email, mobile:data.mobile,
      houseStreet:data.houseStreet, barangay:data.barangay, city:data.city, province:data.province,
      password:data.password, agree:!!data.agree });
    if(!r.ok){
      UI.signupDraft = data;
      UI.errors = r.status === 409 ? { email:r.error } : {};
      if(r.status !== 409) showToast(r.error);
      render(); return;
    }
    UI.errors = {};
    UI._signupEmail = data.email;
    UI._devCode = r.data.devCode || null;
    UI.authScreen = "signupSuccess";
    render();
  } finally { UI._busy = false; }
}
function goToVerify(){
  UI.authScreen = "verify";
  UI.errors = {};
  render();
}
async function submitVerify(code){
  if(!UI._signupEmail){ UI.authScreen = "login"; render(); return; }
  if(UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/verify", { email:UI._signupEmail, code });
    if(!r.ok){ UI.errors = { verify:r.error }; render(); return; }
    UI.errors = {};
    UI._devCode = null;
    UI.authScreen = "verifySuccess";
    render();
  } finally { UI._busy = false; }
}
async function resendSignupVerification(){
  if(!UI._signupEmail || UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/resend-code", {email:UI._signupEmail});
    if(!r.ok){ UI.errors={verify:r.error}; render(); return; }
    UI.errors={};
    render();
    showToast("A new verification code was sent to your email.");
  } finally { UI._busy=false; }
}

function startForgot(){
  UI.forgotDraft = {email:"", code:"", newPw:"", confirm:""};
  UI.authScreen = "forgotEmail";
  UI.errors = {};
  render();
}
async function forgotSendCode(email){
  const e = String(email || "").trim();
  if(!e){ UI.errors = {forgot:"Please enter your email."}; render(); return; }
  if(UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/forgot", { email:e });
    if(!r.ok){ UI.errors = {forgot:r.error}; render(); return; }
    UI._forgotEmail = e;
    UI._devCode = r.data.devCode || null;
    UI.errors = {};
    UI.authScreen = "forgotCode";            // same screen whether or not the email has an account
    render();
  } finally { UI._busy = false; }
}
async function forgotVerifyCode(code){
  if(UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/reset/check", { email:UI._forgotEmail, code });
    if(!r.ok){ UI.errors = {forgot:r.error}; render(); return; }
    UI._forgotCode = code;
    UI.errors = {};
    UI.authScreen = "forgotNewPw";
    render();
  } finally { UI._busy = false; }
}
async function forgotSetPassword(pw, confirm){
  if(!pw || pw.length < 8){ UI.errors = {forgot:"Password must be at least 8 characters"}; render(); return; }
  if(!/[A-Za-z]/.test(pw) || !/[0-9]/.test(pw)){ UI.errors = {forgot:"Password must contain a letter and a number"}; render(); return; }
  if(pw !== confirm){ UI.errors = {forgot:"Password mismatch"}; render(); return; }
  if(UI._busy) return;
  UI._busy = true;
  try{
    const r = await Api.post("/api/auth/reset", { email:UI._forgotEmail, code:UI._forgotCode, newPassword:pw });
    if(!r.ok){ UI.errors = {forgot:r.error}; render(); return; }
    UI._forgotCode = null; UI._devCode = null;
    UI.errors = {};
    UI.authScreen = "forgotSuccess";
    render();
  } finally { UI._busy = false; }
}

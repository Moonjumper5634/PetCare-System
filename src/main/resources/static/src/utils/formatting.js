/* PetCARE — Date, name and text formatting helpers */

/* ---------------- DATE / FORMAT HELPERS ---------------- */
function fmtDate(d){
  if(!d) return "—";
  const dt = (d instanceof Date) ? d : new Date(d+"T00:00:00");
  if(isNaN(dt)) return "—";
  return dt.toLocaleDateString("en-US",{month:"short", day:"numeric", year:"numeric"});
}
function fmtDateTime(d){
  const dt = (d instanceof Date) ? d : new Date(d);
  return dt.toLocaleDateString("en-US",{month:"short", day:"numeric", year:"numeric"}) + " " +
         dt.toLocaleTimeString("en-US",{hour:"2-digit", minute:"2-digit"});
}
function monthsBetween(dateStr, ref){
  const d = new Date(dateStr+"T00:00:00");
  const r = ref || TODAY;
  return (r.getFullYear()-d.getFullYear())*12 + (r.getMonth()-d.getMonth()) + (r.getDate()>=d.getDate()?0:-1);
}
function ageString(dob){
  const m = monthsBetween(dob);
  if(m < 1) return "Newborn";
  if(m < 12) return m + "m";
  const y = Math.floor(m/12), rem = m%12;
  return rem ? `${y}y ${rem}m` : `${y}y`;
}
function todayISO(){
  const d = TODAY, p = n => String(n).padStart(2, "0");   // local date parts: toISOString() would shift to UTC (16 Aug in Manila)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function initials(name){
  return (name||"").split(" ").filter(Boolean).map(w=>w[0]).slice(0,2).join("").toUpperCase();
}

function esc(s){
  return String(s==null?"":s).replace(/[&<>"']/g, c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
}

/* Role display label — resident-facing terminology (Owner role -> "Resident"). */
function roleDisplay(role){
  return role==="owner" ? "Resident" : role==="staff" ? "CVO Staff" : "Administrator";
}

/* PetCARE — Resident / user data access
   FUTURE BACKEND: getUserById -> GET /api/users/:id, getResidents -> GET /api/residents,
   searchResidents -> GET /api/residents?q=, createResident -> POST /api/residents */
const ResidentService = {
  getUserById(id){ return S.users.find(u => u.id === id); },
  getResidents(){ return S.users.filter(u => u.role === "owner"); },
  searchResidents(q){
    q = String(q || "").toLowerCase();
    return this.getResidents().filter(u => u.name.toLowerCase().includes(q) || (u.mobile || "").includes(q) || u.email.toLowerCase().includes(q));
  },
};

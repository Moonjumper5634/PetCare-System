/* PetCARE — Application constants (dates, barangays, services, storage key) */

/* =========================================================================
   PetCARE — City Veterinary Office, Santa Rosa, Laguna
   Revised prototype: real state, role-based navigation, multi-step
   workflows, service-specific eligibility, staff decision separation,
   walk-in flow, adoption flow, notifications & audit logs.
   ========================================================================= */

var TODAY = (function(){ const n = new Date(); return new Date(n.getFullYear(), n.getMonth(), n.getDate()); })(); // replaced by the server's date at sign-in (hydrate); the dev server pins it to 17 Aug 2026
const BARANGAYS = ["Balibago","Pooc","Dita","Ibaba","Sinalhan","Outside Santa Rosa"];
const SERVICES = ["Anti-Rabies Vaccination","Microchipping","Kapon / Spay-Neuter"];
const HOUSING_TYPES = ["Owned House","Rented House/Apartment","Condominium","Living with Family"];

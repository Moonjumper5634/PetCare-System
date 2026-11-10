/* PetCARE — Veterinary service data access (registrations, service requests, adoption)
   FUTURE BACKEND: GET /api/registrations/:id, GET /api/service-requests/:id,
   GET /api/adoptions/:id, GET /api/shelter-pets/:id */
const VeterinaryService = {
  getServiceCatalog(){ return SERVICES; },
  getRegistrationById(id){ return S.registrations.find(r => r.id === id); },
  getRequestById(id){ return S.requests.find(r => r.id === id); },
  getAdoptionById(id){ return S.adoptions.find(a => a.id === id); },
  getShelterPetById(id){ return S.shelterPets.find(s => s.id === id); },
};

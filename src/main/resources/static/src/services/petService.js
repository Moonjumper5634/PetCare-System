/* DogCARE — Pet data access
   FUTURE BACKEND: getPets -> GET /api/pets, getPetById -> GET /api/pets/:id,
   getPetsByOwner -> GET /api/pets?ownerId= */
const PetService = {
  getPets(){ return S.pets.filter(p => !p.archived); },
  getPetById(id){ return S.pets.find(p => p.id === id); },
  getPetsByOwner(ownerId){ return S.pets.filter(p => p.ownerId === ownerId && !p.archived); },
  getVerifiedPetsByOwner(ownerId){ return this.getPetsByOwner(ownerId).filter(p => p.registrationStatus === "Verified"); },
};

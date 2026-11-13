/* PetCARE — Screen registry (screen id -> renderer) */

function screenDashboard(){
  const role = currentUser().role;
  if(role==="owner") return screenDashboardOwner();
  if(role==="staff") return screenDashboardStaff();
  return screenDashboardAdmin();
}

/* ---------------- SCREEN MAP ---------------- */
const screenMap = {
  dashboard: screenDashboard,
  mypets: screenMyPets,
  petprofile: screenPetProfile,
  petregistration: ()=>screenPetRegistration(),
  requestservice: ()=>screenRequestService(),
  myrequests: screenMyRequests,
  adopt: screenAdopt,
  adoptdetail: screenAdoptDetail,
  adoptapply: screenAdoptApply,
  adoptsubmitted: screenAdoptSubmitted,
  notifications: screenNotifications,
  profile: screenProfile,
  petregistrations: screenPetRegistrations,
  regdetail: screenRegDetail,
  servicerequests: screenServiceRequests,
  eligibility: screenEligibilityQueue,
  reqdetail: screenReqDetail,
  adoptionapplications: screenAdoptionApplications,
  adoptreview: screenAdoptReview,
  servicesrecords: screenServicesRecords,
  walkin: screenWalkin,
  reports: screenReports,
  auditlogs: screenAuditLogs,
  usermanagement: screenUsers,
  petrecords: screenPetRecordsAdmin,
  servicemanagement: screenServiceManagement,
  eligibilitycriteria: screenEligibilityCriteria,
  systemsettings: screenSystemSettings,
  faq: screenFAQ,
};

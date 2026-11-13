/* PetCARE — Role-based navigation menus */

/* ---------------- ROLE NAVIGATION (per revision spec section 3) ---------------- */
const navs = {
  owner: [
    {group:"Main"},
    {id:"dashboard", label:"Dashboard"},
    {id:"mypets", label:"My Pets"},
    {id:"petregistration", label:"Register a Pet"},
    {id:"requestservice", label:"Request a Service"},
    {id:"myrequests", label:"My Requests"},
    {id:"adopt", label:"Adopt a Pet"},
    {group:"Account"},
    {id:"profile", label:"Profile"},
    {id:"faq", label:"FAQ / Help"},
  ],
  staff: [
    {group:"Main"},
    {id:"dashboard", label:"Dashboard"},
    {group:"Registrations & Requests"},
    {id:"petregistrations", label:"Pet Registrations"},
    {id:"servicerequests", label:"Service Requests"},
    {id:"eligibility", label:"Eligibility Assessment"},
    {group:"Adoption"},
    {id:"adoptionapplications", label:"Adoption Applications"},
    {group:"Services"},
    {id:"servicesrecords", label:"Services & Records"},
    {id:"walkin", label:"Walk-in Resident"},
    {group:"Account"},
    {id:"profile", label:"Profile"},
  ],
  admin: [
    {group:"Main"},
    {id:"dashboard", label:"Dashboard"},
    {group:"Administration"},
    {id:"usermanagement", label:"User Management"},
    {id:"petrecords", label:"Pet Records"},
    {id:"servicemanagement", label:"Service Management"},
    {id:"eligibilitycriteria", label:"Eligibility Criteria"},
    {group:"Reporting"},
    {id:"reports", label:"Reports"},
    {id:"auditlogs", label:"Action History"},
    {group:"System"},
    {id:"systemsettings", label:"System Settings"},
    {id:"notifications", label:"Notifications"},
    {id:"profile", label:"Profile"},
  ]
};

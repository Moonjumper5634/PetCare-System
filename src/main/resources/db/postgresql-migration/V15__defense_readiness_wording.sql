UPDATE eligibility_criterion
   SET description = 'Configured demonstration threshold: at least 3 kg. This is decision support only; authorized CVO staff must confirm the operational requirement and make the final decision.'
 WHERE service_name = 'Kapon / Spay-Neuter' AND criterion_key = 'weight';

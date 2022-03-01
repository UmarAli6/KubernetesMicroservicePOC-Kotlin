CREATE TABLE J_TRANSPORT (
    ID          INT NOT NULL,
    PO_DOCUMENT CLOB,
    CONSTRAINT ENSURE_JSON CHECK (PO_DOCUMENT IS JSON)
);
    
INSERT INTO j_transport
VALUES (
    1,
    '{
 "user": "Alex",
 "newValue": {
 "jobValidityDuration": "P5D",
 "sms": {
 "smOtaValidityDuration": "PT6H"
 },
 "httpOta": {
 "smHttpTrigEnabled": false,
 "smHttpTrigValidityDuration": "PT6H",
 "smHttpTrigBlacklistedCardProfiles": []
 }
 },
 "changeComment": "Changed jobValidity Duration to P6D from P5D."
}');

SELECT PO.PO_DOCUMENT.newValue FROM J_TRANSPORT PO;
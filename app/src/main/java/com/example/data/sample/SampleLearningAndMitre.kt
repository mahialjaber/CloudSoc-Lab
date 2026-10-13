package com.example.data.sample

import com.example.model.LearningTopic
import com.example.model.MitreTechnique

object SampleLearningAndMitre {

    fun getMitreMatrix(): List<MitreTechnique> {
        return listOf(
            MitreTechnique(
                id = "T1078.004",
                name = "Valid Accounts: Cloud Accounts",
                tactic = "Initial Access",
                description = "Adversaries may obtain and abuse credentials of existing cloud accounts, accessing AWS Management Console or invoking APIs directly using long-lived AWS IAM Access Keys or STS session tokens.",
                cloudDetectionQuery = "eventSource:iam.amazonaws.com AND (eventName:ConsoleLogin OR eventName:GetCallerIdentity) AND recipientAccountId != userIdentity.accountId",
                mitigation = "Enforce Multi-Factor Authentication (MFA) on all IAM users, set maximum session duration on IAM roles to 1 hour, and rotate long-lived programmatic keys every 90 days."
            ),
            MitreTechnique(
                id = "T1190",
                name = "Exploit Public-Facing Application",
                tactic = "Initial Access",
                description = "Adversaries may exploit vulnerabilities (such as SSRF, Command Injection, or SQLi) in public web applications hosted on EC2 or Lambda to gain an initial foothold or query internal metadata endpoints.",
                cloudDetectionQuery = "source:AWS_WAF AND (ruleGroupList.terminatingRule.ruleId:*SSRF* OR httpRequest.uri:*169.254.169.254*)",
                mitigation = "Enforce IMDSv2 (HttpTokens=required) on all EC2 instances, apply AWS WAF managed rule sets, and implement rigorous input URL validation."
            ),
            MitreTechnique(
                id = "T1110.001",
                name = "Brute Force: Password Guessing",
                tactic = "Initial Access",
                description = "Adversaries may systematically guess passwords against internet-exposed SSH/RDP services or cloud identity endpoints to gain initial access.",
                cloudDetectionQuery = "source:auditd AND (result:\"failed\" OR message:\"Failed password\") | stats count by src_ip having count > 20",
                mitigation = "Enforce fail2ban, SSH key-only authentication, and AWS Security Group restrictions on port 22."
            ),
            MitreTechnique(
                id = "T1059.001",
                name = "Command and Scripting Interpreter: PowerShell",
                tactic = "Execution",
                description = "Adversaries may execute encoded or obfuscated PowerShell commands with bypass flags (-enc, -Exec Bypass, -NoP) to download secondary payloads or execute memory-only scripts.",
                cloudDetectionQuery = "source:Sysmon AND EventID:1 AND (CommandLine:*powershell* AND (CommandLine:*-enc* OR CommandLine:*-exec bypass* OR CommandLine:*downloadstring*))",
                mitigation = "Enable PowerShell Script Block Logging (Event ID 4104), Transcription Logging, and enforce AppLocker in Constrained Language Mode."
            ),
            MitreTechnique(
                id = "T1059.004",
                name = "Command and Scripting Interpreter: Unix Shell",
                tactic = "Execution",
                description = "Adversaries may execute bash/sh commands, piping downloaded scripts directly into execution (e.g. curl ... | bash) to bypass file-based disk scanning.",
                cloudDetectionQuery = "source:auditd AND type:EXECVE AND (a0:\"curl\" OR a0:\"wget\") AND (a1:\"| sh\" OR a1:\"| bash\" OR a2:\"/dev/tcp/\")",
                mitigation = "Deploy Linux runtime monitoring (e.g. Falco, auditd), restrict outbound HTTP egress for non-proxy service accounts, and mount /tmp with noexec."
            ),
            MitreTechnique(
                id = "T1098.001",
                name = "Account Manipulation: Additional Cloud Credentials",
                tactic = "Persistence",
                description = "Adversaries may create new IAM access keys, upload SSH keys to AWS IAM users, or create backdoor IAM users to maintain redundant persistence.",
                cloudDetectionQuery = "eventSource:iam.amazonaws.com AND (eventName:CreateAccessKey OR eventName:UploadSSHPublicKey OR eventName:CreateLoginProfile)",
                mitigation = "Enforce AWS Service Control Policies (SCPs) preventing iam:CreateAccessKey without a dedicated administrator break-glass role."
            ),
            MitreTechnique(
                id = "T1053.003",
                name = "Scheduled Task/Job: Systemd Service",
                tactic = "Persistence",
                description = "Adversaries may create or modify systemd services in /etc/systemd/system/ to execute malicious binaries on boot or restart.",
                cloudDetectionQuery = "source:auditd AND (name:*systemd/system* OR name:*cron.d*) AND nametype:CREATE",
                mitigation = "Use file integrity monitoring (FIM) on /etc/systemd and /etc/cron*, and set read-only root filesystems on immutable container nodes."
            ),
            MitreTechnique(
                id = "T1548.005",
                name = "Abuse Elevation Control Mechanism: Temporary Elevated Cloud Access",
                tactic = "Privilege Escalation",
                description = "Adversaries may attach AdministratorAccess policies to their own IAM user or modify IAM role trust policies to assume higher privilege roles.",
                cloudDetectionQuery = "eventSource:iam.amazonaws.com AND (eventName:AttachUserPolicy OR eventName:AttachRolePolicy) AND requestParameters.policyArn:*AdministratorAccess*",
                mitigation = "Implement AWS IAM Permissions Boundaries on all non-admin roles and alert instantly on privileged policy attachment events."
            ),
            MitreTechnique(
                id = "T1098",
                name = "Account Manipulation: Role Trust Tampering",
                tactic = "Privilege Escalation",
                description = "Adversaries may modify trust policies on existing IAM roles to allow external adversary accounts or unauthorized identities to assume them.",
                cloudDetectionQuery = "eventSource:iam.amazonaws.com AND (eventName:UpdateAssumeRolePolicy OR eventName:PutRolePolicy)",
                mitigation = "Monitor UpdateAssumeRolePolicy events in CloudTrail and enforce SCPs restricting external account trust relationships."
            ),
            MitreTechnique(
                id = "T1562.001",
                name = "Impair Defenses: Disable Cloud Security Controls",
                tactic = "Defense Evasion",
                description = "Adversaries may stop CloudTrail logging (StopLogging, DeleteTrail), disable GuardDuty detectors, or modify S3 Bucket Policies to public.",
                cloudDetectionQuery = "eventSource:cloudtrail.amazonaws.com AND (eventName:StopLogging OR eventName:DeleteTrail OR eventName:UpdateDetector)",
                mitigation = "Apply SCPs preventing deletion or modification of CloudTrail, GuardDuty, and Config across all member accounts."
            ),
            MitreTechnique(
                id = "T1055",
                name = "Process Injection",
                tactic = "Defense Evasion",
                description = "Adversaries may inject code into processes in order to evade process-based defenses as well as possibly elevate privileges.",
                cloudDetectionQuery = "source:Sysmon AND EventID:8 AND (SourceImage:*powershell* OR TargetImage:*svchost*)",
                mitigation = "Implement endpoint detection and response (EDR) memory scanning and restrict debug privileges."
            ),
            MitreTechnique(
                id = "T1003.001",
                name = "OS Credential Dumping: LSASS Memory",
                tactic = "Credential Access",
                description = "Adversaries may attempt to read the memory of Local Security Authority Server Service (LSASS) to harvest plaintext passwords or Kerberos tickets.",
                cloudDetectionQuery = "source:Sysmon AND EventID:10 AND TargetImage:*lsass.exe AND (GrantedAccess:0x1010 OR GrantedAccess:0x1F3FFF)",
                mitigation = "Enable Windows Defender Credential Guard (LSA Protection) and RunAsPPL on Windows endpoints."
            ),
            MitreTechnique(
                id = "T1552.005",
                name = "Unsecured Credentials: Cloud Instance Metadata Service",
                tactic = "Credential Access",
                description = "Adversaries query the AWS IMDS (169.254.169.254) from within a compromised EC2 host to extract temporary IAM instance profile credentials.",
                cloudDetectionQuery = "source:CloudTrail AND userIdentity.type:AssumedRole AND sourceIPAddress NOT IN (PrivateVpcCIDRs)",
                mitigation = "Migrate to IMDSv2 with token hop limit 1 and enforce ec2:MetadataHttpEndpoint=enabled with ec2:MetadataHttpTokens=required."
            ),
            MitreTechnique(
                id = "T1530",
                name = "Data from Cloud Storage",
                tactic = "Collection",
                description = "Adversaries may access, list, or download sensitive data stored in cloud object storage repositories like Amazon S3.",
                cloudDetectionQuery = "eventSource:s3.amazonaws.com AND (eventName:GetObject OR eventName:ListObjectsV2) AND bytesSent > 1000000000",
                mitigation = "Enable S3 Block Public Access at the organization root, enforce KMS SSE-KMS customer-managed keys, and enable S3 object-level CloudTrail logging."
            ),
            MitreTechnique(
                id = "T1537",
                name = "Transfer Data to Cloud Account",
                tactic = "Exfiltration",
                description = "Adversaries may share snapshots (RDS, EBS) or replicate S3 buckets directly to an external, adversary-controlled cloud account.",
                cloudDetectionQuery = "eventName:ModifyDBSnapshotAttribute OR eventName:ModifyImageAttribute AND requestParameters.valuesToAdd:*",
                mitigation = "Enforce SCPs disallowing snapshot sharing across AWS Organization boundaries."
            ),
            MitreTechnique(
                id = "T1041",
                name = "Exfiltration Over C2 Channel",
                tactic = "Exfiltration",
                description = "Adversaries may steal data by transferring it over an existing command and control channel rather than over an alternate path.",
                cloudDetectionQuery = "source:VPC_Flow AND dest_port:443 AND bytes > 500000000 AND (dest_ip:* OR action:ACCEPT)",
                mitigation = "Enforce egress filtering and proxy inspection for all outbound traffic."
            ),
            MitreTechnique(
                id = "T1496",
                name = "Resource Hijacking: Compute Resources",
                tactic = "Impact",
                description = "Adversaries may hijack EC2 or ECS instances to execute cryptocurrency mining software (e.g. XMRig) consuming compute resources.",
                cloudDetectionQuery = "source:GuardDuty AND findingType:*CryptoCurrency:EC2/BitcoinTool.B!DNS*",
                mitigation = "Use AWS GuardDuty Runtime Monitoring, restrict outbound network egress with AWS Network Firewall, and monitor CPU utilization anomalies."
            )
        )
    }

    fun getLearningPaths(): List<LearningTopic> {
        return listOf(
            LearningTopic(
                id = "learn-101",
                tierNumber = 1,
                tierTitle = "Tier 1: SOC Fundamentals",
                title = "SIEM Log Triage & Core Event Sources",
                category = "SIEM & Logging",
                estimatedMinutes = 15,
                summary = "Understand the critical differences between CloudTrail, Windows Event Logs (Security/Sysmon), Linux auditd, and VPC Flow Logs. Learn how to filter out baseline noise.",
                keyTakeaways = listOf(
                    "CloudTrail records management plane API actions (who did what, when, from where).",
                    "Sysmon provides kernel-level endpoint visibility (Event ID 1: Process Creation, ID 3: Network, ID 10: ProcessAccess).",
                    "VPC Flow Logs record network packet metadata (Src IP, Dst IP, Src Port, Dst Port, Protocol, Action: ACCEPT/REJECT)."
                ),
                detectionLogicSnippet = "EventSource == \"iam.amazonaws.com\" | where eventName in (\"CreateAccessKey\", \"AttachUserPolicy\") | project eventTime, userIdentity.arn, sourceIPAddress",
                sampleLogHighlight = "{\"eventSource\":\"iam.amazonaws.com\", \"eventName\":\"AttachUserPolicy\", \"policyArn\":\"arn:aws:iam::aws:policy/AdministratorAccess\"}",
                practicalCheckQuestion = "Which Sysmon event ID indicates an attempt to access process memory (e.g. LSASS dumping)?",
                practicalCheckAnswer = "Sysmon Event ID 10 (ProcessAccess) with GrantedAccess flags like 0x1010."
            ),
            LearningTopic(
                id = "learn-102",
                tierNumber = 1,
                tierTitle = "Tier 1: SOC Fundamentals",
                title = "Windows Sysmon & Process Tree Forensics",
                category = "Windows Security",
                estimatedMinutes = 20,
                summary = "Master process parentage analysis in Windows. Learn how adversary scripts spawn from Office macros (WINWORD -> powershell) and how process hollowing occurs.",
                keyTakeaways = listOf(
                    "Normal applications rarely spawn powershell.exe or cmd.exe directly from WINWORD.EXE or EXCEL.EXE.",
                    "Look for command-line evasion flags: -ExecutionPolicy Bypass, -WindowStyle Hidden, -EncodedCommand.",
                    "Verify file hashes (SHA256) of binaries dropped in writable public directories (C:\\Users\\Public\\, C:\\Windows\\Temp\\)."
                ),
                detectionLogicSnippet = "Sysmon | where EventID == 1 and ParentImage endswith \"WINWORD.EXE\" and Image endswith \"powershell.exe\"",
                sampleLogHighlight = "ParentImage: WINWORD.EXE -> Image: powershell.exe -enc SQBFAFgA...",
                practicalCheckQuestion = "Why do attackers encode PowerShell commands with Base64 (-enc)?",
                practicalCheckAnswer = "To bypass naive string-matching detection rules and conceal malicious payload URLs or staging commands."
            ),
            LearningTopic(
                id = "learn-201",
                tierNumber = 2,
                tierTitle = "Tier 2: Cloud SOC Triage",
                title = "AWS IAM Privilege Escalation Vectors",
                category = "Cloud IAM Security",
                estimatedMinutes = 25,
                summary = "Explore how attackers escalate privileges in AWS IAM using permissions like iam:AttachUserPolicy, iam:CreatePolicyVersion, and sts:AssumeRole.",
                keyTakeaways = listOf(
                    "iam:AttachUserPolicy with AdministratorAccess is the most direct privilege escalation vector.",
                    "iam:CreatePolicyVersion allows an adversary to set a new default policy document with wildcard '*' rights.",
                    "Cross-account AssumeRole policies must be strictly scoped to trusted AWS Account IDs."
                ),
                detectionLogicSnippet = "CloudTrail | where eventSource == \"iam.amazonaws.com\" and eventName in (\"AttachUserPolicy\", \"PutUserPolicy\", \"CreatePolicyVersion\") and requestParameters contains \"AdministratorAccess\"",
                sampleLogHighlight = "eventName: AttachUserPolicy, policyArn: arn:aws:iam::aws:policy/AdministratorAccess",
                practicalCheckQuestion = "What IAM guardrail prevents users from attaching policies exceeding a specified permission boundary?",
                practicalCheckAnswer = "AWS IAM Permissions Boundaries (iam:PermissionsBoundary), which enforce an absolute maximum ceiling of allowable permissions."
            ),
            LearningTopic(
                id = "learn-202",
                tierNumber = 2,
                tierTitle = "Tier 2: Cloud SOC Triage",
                title = "Amazon S3 Data Security & Exfiltration Forensics",
                category = "Cloud Storage",
                estimatedMinutes = 20,
                summary = "Learn how to detect bucket policy tampering, public ACL modifications, and correlate S3 server access logs to calculate stolen data volume.",
                keyTakeaways = listOf(
                    "Adversaries tamper with PutBucketPolicy to grant Principal: * public read access.",
                    "S3 Server Access Logs capture object-level GET requests, total bytes transferred, and remote client IPs.",
                    "Enable S3 Block Public Access at both the Bucket and Account level to override dangerous policies."
                ),
                detectionLogicSnippet = "S3AccessLogs | where httpStatus == 200 and bytesSent > 104857600 | summarize TotalBytes = sum(bytesSent) by remoteIp, bucket",
                sampleLogHighlight = "eventName: PutBucketPolicy, Principal: *, Action: s3:GetObject",
                practicalCheckQuestion = "How do you immediately block public access to an S3 bucket without deleting the existing policy?",
                practicalCheckAnswer = "Enable S3 Block Public Access (BlockPublicAcls, IgnorePublicAcls, BlockPublicPolicy, RestrictPublicBuckets) on the bucket or AWS account."
            ),
            LearningTopic(
                id = "learn-301",
                tierNumber = 3,
                tierTitle = "Tier 3: Advanced Threat Hunting",
                title = "EC2 SSRF & IMDSv1 Credential Pivoting",
                category = "Cloud Attack Chains",
                estimatedMinutes = 30,
                summary = "Deep dive into Server-Side Request Forgery against the EC2 Instance Metadata Service (IMDSv1) and how attackers use stolen STS tokens outside the VPC.",
                keyTakeaways = listOf(
                    "IMDSv1 allows unauthenticated HTTP GET requests to http://169.254.169.254/latest/meta-data/iam/security-credentials/<RoleName>.",
                    "IMDSv2 requires a session token via PUT request (X-aws-ec2-metadata-token-ttl-seconds: 21600), mitigating SSRF.",
                    "GuardDuty flags instance credentials used from outside the AWS network."
                ),
                detectionLogicSnippet = "CloudTrail | where userIdentity.type == \"AssumedRole\" and sourceIPAddress !startswith \"10.\" and sourceIPAddress !startswith \"172.16.\" and sourceIPAddress !startswith \"192.168.\"",
                sampleLogHighlight = "GET /api/proxy?url=http://169.254.169.254/latest/meta-data/iam/security-credentials/EC2AppRole",
                practicalCheckQuestion = "What configuration flag enforces IMDSv2 on an EC2 instance?",
                practicalCheckAnswer = "Setting HttpTokens to 'required' (e.g. aws ec2 modify-instance-metadata-options --http-tokens required)."
            ),
            LearningTopic(
                id = "learn-401",
                tierNumber = 4,
                tierTitle = "Tier 4: Incident Commander",
                title = "SOC Incident Response Playbooks & Containment Strategy",
                category = "Incident Response",
                estimatedMinutes = 30,
                summary = "Learn how senior incident responders evaluate blast radius, execute surgical containment, and write executive-ready SOC incident reports.",
                keyTakeaways = listOf(
                    "Prioritize credential revocation and network isolation over destructive actions (never terminate production databases blindly).",
                    "Preserve evidence (EBS snapshots, memory dumps) prior to remediation.",
                    "Structure incident reports with Executive Summary, Timeline, IOC Table, Root Cause, and Lessons Learned."
                ),
                detectionLogicSnippet = "Incident Timeline: Initial Ingress -> Execution -> Persistence -> PrivEsc -> Collection -> Exfiltration -> Containment",
                sampleLogHighlight = "Containment Playbook: 1. Revoke STS Sessions -> 2. Quarantine Security Group -> 3. Block C2 IP -> 4. Rotate IAM Keys",
                practicalCheckQuestion = "What is the danger of terminating a compromised EC2 instance during live incident response?",
                practicalCheckAnswer = "Terminating the instance destroys ephemeral volatile RAM memory, open network sockets, and temporary staging artifacts needed for root cause forensics."
            )
        )
    }
}

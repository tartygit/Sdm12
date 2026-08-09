import React, { useState, useEffect } from 'react';
import {
  FolderCheck, Sparkles, CheckCircle, AlertTriangle, HelpCircle,
  Send, RefreshCw, Layers, ShieldAlert, Award, FileText, ChevronRight,
  UserCheck, Sliders, ToggleLeft, ToggleRight, Download, Search, MessageSquare
} from 'lucide-react';

const PHASES = [
  { num: 1, name: "Phase 1: Requirements Gathering", deliverables: [
    { id: "P001", title: "Business Requirement Document (BRD)", desc: "Defines business needs, scope, and objectives." },
    { id: "P002", title: "Functional Specification Document (FSD)", desc: "Detailed functional behaviors, workflows, and specifications." },
    { id: "P003", title: "Use Case Specification (UCS)", desc: "Actor interaction flows and step-by-step use scenarios." }
  ]},
  { num: 2, name: "Phase 2: System Design", deliverables: [
    { id: "P004", title: "High Level Design Document (HLD)", desc: "Overall system architecture, modules, and interfaces." },
    { id: "P005", title: "Detailed Level Design Document (DLD)", desc: "Component specifications, class designs, and sequencing." },
    { id: "P006", title: "Database Design Document (DDD)", desc: "Data models, entity relationships, schema tables, and dictionaries." }
  ]},
  { num: 3, name: "Phase 3: Development & Unit Testing", deliverables: [
    { id: "P007", title: "Unit Test Plan (UTP)", desc: "Strategy, setups, and scenarios for unit level validation." },
    { id: "P008", title: "Unit Test Report (UTR)", desc: "Results and metrics of executed unit tests." }
  ]},
  { num: 4, name: "Phase 4: Integration Testing", deliverables: [
    { id: "P009", title: "System Integration Test Plan (SITP)", desc: "Strategy for validating composite interfaces." },
    { id: "P010", title: "System Integration Test Report (SITR)", desc: "Logs, execution findings, and results of SIT." }
  ]},
  { num: 5, name: "Phase 5: User Acceptance Testing", deliverables: [
    { id: "P011", title: "User Acceptance Test Plan (UATP)", desc: "Strategy and test cases for business users check." },
    { id: "P012", title: "User Acceptance Test Report (UATR)", desc: "Business signoff logs, outcomes, and business clearance." }
  ]},
  { num: 6, name: "Phase 6: Deployment & Go-Live", deliverables: [
    { id: "P013", title: "Deployment Plan (DP)", desc: "Release checklist, server configurations, rollback plans." },
    { id: "P014", title: "Operations Manual (OM)", desc: "Sysadmin running logs, backups, support paths, and diagnostics." }
  ]},
  { num: 7, name: "Phase 7: Post Go-Live Support & Closure", deliverables: [
    { id: "P015", title: "Post Implementation Review (PIR)", desc: "Project performance check, learnings, and user response." },
    { id: "P016", title: "Project Closure Report (PCR)", desc: "Formal signoff sheet, handovers, and close milestones." }
  ]}
];

export default function App() {
  // App Config states
  const [appName, setAppName] = useState("Software Development Document Environment");
  const [appCode, setAppCode] = useState("PRJ");
  const [currentDeliverables, setCurrentDeliverables] = useState(PHASES);

  // Simulation switches
  const [role, setRole] = useState("MAKER"); // MAKER, CHECKER
  const [mfaEnabled, setMfaEnabled] = useState(false);
  const [alertsEnabled, setAlertsEnabled] = useState(false);
  const [authStrategy, setAuthStrategy] = useState("DB"); // DB, LDAP

  // Local state
  const [selectedDoc, setSelectedDoc] = useState(PHASES[0].deliverables[0]);
  const [uploadFile, setUploadFile] = useState(null);
  const [version, setVersion] = useState("V1.0");
  const [docCode, setDocCode] = useState("DOC-001");
  const [successMsg, setSuccessMsg] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  // Submissions catalog mock db
  const [submissions, setSubmissions] = useState([
    {
      id: 101,
      docId: "P001",
      appCode: "PRJ",
      fileName: "PRJ_P001_V1.0_BRD-001.xlsx",
      version: "V1.0",
      code: "BRD-001",
      status: "APPROVED",
      maker: "maker",
      checker: "checker",
      submittedAt: "2026-08-08 14:30:10",
      parsed: {
        "Introduction": "Defines overall project scope for modern SDLC deliverables tracking...",
        "Business Requirements": "Must operate completely offline once models are downloaded, featuring persistent vector embeddings."
      }
    }
  ]);

  // AI analysis panel states
  const [aiReport, setAiReport] = useState({
    summary: "Select a deliverable or upload a document to generate instant automated local AI assessments.",
    missing_sections: ["Scope Definition", "Compliance Sign-off"],
    compliance_score: 85,
    suggested_improvements: ["Introduce strict actor workflow mapping", "Detail integration constraints"],
    similar_documents: ["PRJ_P002_V1.1_FSD.docx"],
    risk_assessment: "MEDIUM Risk. Minor gaps in architectural validation boundaries.",
    quality_score: 90,
    version_comparison: "Compared with V1.0. Quality coverage has improved by 15% due to section additions.",
    duplicate_detection: "No duplicate records matched in FAISS vector catalog."
  });
  const [isAnalyzing, setIsAiAnalyzing] = useState(false);

  // Chat/RAG States
  const [chatInput, setChatInput] = useState("");
  const [chatHistory, setChatHistory] = useState([
    { role: "assistant", content: "Hello! I am your local SDM AI assistant. Ask me anything about your uploaded SDLC documents or system standards." }
  ]);
  const [isChatLoading, setIsChatLoading] = useState(false);
  const [activeCitations, setActiveCitations] = useState([]);

  // Auto Poller Directory drops simulation
  const [pollFiles, setPollFiles] = useState([]);
  const [pollerLog, setPollerLog] = useState(["[SYSTEM] Poller listening on directory /input-documents..."]);

  // Set selected deliverable
  const handleSelectDoc = (doc) => {
    setSelectedDoc(doc);
    // Simulate fetching updated AI Report for document
    setIsAiAnalyzing(true);
    setTimeout(() => {
      setAiReport({
        summary: `concise AI-generated summary of deliverable ${doc.id} (${doc.title}). Fully parsing standard schemas.`,
        missing_sections: doc.id === "P001" ? ["Stakeholders Analysis"] : ["Database Indexes Dictionary", "Data Hashing Specs"],
        compliance_score: doc.id === "P001" ? 95 : 75,
        suggested_improvements: [`Introduce detailed checklists for ${doc.id} schema verification`, "Clarify unit level assertion scopes."],
        similar_documents: ["PRJ_P001_V1.0_BRD-001.xlsx"],
        risk_assessment: doc.id === "P001" ? "LOW Risk. Standard layout guidelines perfectly followed." : "MEDIUM Risk. Missing expected schema foreign keys outlines.",
        quality_score: doc.id === "P001" ? 92 : 80,
        version_comparison: "Baseline Version compared with system deliverable templates. Clean sequence detected.",
        duplicate_detection: "No duplicate document matched inside local FAISS database."
      });
      setIsAiAnalyzing(false);
    }, 600);
  };

  // Submit file manually
  const handleSubmitFile = (e) => {
    e.preventDefault();
    if (!uploadFile) {
      setErrorMsg("Please select a file to upload.");
      return;
    }

    const newSub = {
      id: Date.now(),
      docId: selectedDoc.id,
      appCode: appCode.toUpperCase(),
      fileName: `${appCode.toUpperCase()}_${selectedDoc.id}_${version}_${docCode}.${uploadFile.name.split('.').pop()}`,
      version: version,
      code: docCode,
      status: "PENDING_APPROVAL",
      maker: "maker",
      checker: null,
      submittedAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      parsed: {
        "Executive Summary": "Fully ingested text contents from manually uploaded document deliverable.",
        "System Scope Details": "This deliverable presents the core parameters supporting automated tracking."
      }
    };

    setSubmissions([newSub, ...submissions]);
    setSuccessMsg(`Document ${newSub.fileName} submitted successfully for review!`);
    setUploadFile(null);
    setTimeout(() => setSuccessMsg(""), 4000);
  };

  // Directory drop poller simulation
  const handleDropSimulation = () => {
    const randomDoc = PHASES[Math.floor(Math.random() * PHASES.length)].deliverables[0];
    const mockFile = `${appCode.toUpperCase()}_${randomDoc.id}_V2.0_AUTO-POLLED.docx`;

    setPollerLog(prev => [
      `[POLLER] Ingested file drop: ${mockFile}`,
      `[AI PIPELINE] Docling converter successfully parsed layout structure...`,
      `[AI PIPELINE] Chunked and embedded via nomic-embed-text to FAISS index.`,
      `[WORKFLOW] Logged as PENDING Maker-Checker review.`,
      ...prev
    ]);

    const newSub = {
      id: Date.now(),
      docId: randomDoc.id,
      appCode: appCode.toUpperCase(),
      fileName: mockFile,
      version: "V2.0",
      code: "AUTO-POLLED",
      status: "PENDING_APPROVAL",
      maker: "SYSTEM_POLLER",
      checker: null,
      submittedAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      parsed: {
        "Auto Parsed Header": "Local directory poller automatically grabbed and parsed this deliverable.",
        "SDLC Parameters": "Parsed through advanced Docling structures dynamically."
      }
    };

    setSubmissions(prev => [newSub, ...prev]);
  };

  // Approve / Reject workflows
  const handleApprove = (subId) => {
    setSubmissions(submissions.map(s => s.id === subId ? { ...s, status: "APPROVED", checker: "checker" } : s));
    setSuccessMsg("Document has been APPROVED successfully!");
    setTimeout(() => setSuccessMsg(""), 3000);
  };

  const handleReject = (subId) => {
    setSubmissions(submissions.map(s => s.id === subId ? { ...s, status: "REJECTED", checker: "checker" } : s));
    setErrorMsg("Document has been REJECTED and sent back with feedback.");
    setTimeout(() => setErrorMsg(""), 3000);
  };

  // Local RAG Chat submit
  const handleChatSubmit = (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;

    const userMessage = { role: "user", content: chatInput };
    setChatHistory(prev => [...prev, userMessage]);
    setChatInput("");
    setIsChatLoading(true);

    // Simulate streaming and local retrieval augmented generation (RAG)
    setTimeout(() => {
      // Find matching documents in submissions
      const hasBRD = submissions.some(s => s.docId === "P001");
      let reply = "Based on local semantic search in FAISS vector store: ";
      let citations = [];

      if (hasBRD) {
        reply += "Our Business Requirement Document [V1.0 (BRD-001)] defines that this system must support dynamic database strategies (Oracle, Postgres) and run completely offline.";
        citations = [{ file_name: "PRJ_P001_V1.0_BRD-001.xlsx", section: "Business Requirements" }];
      } else {
        reply += "The context matches general SDLC standards. P001 deliverable defines project scope and stakeholders expectations.";
        citations = [{ file_name: "Baseline_Template.xml", section: "Scope" }];
      }

      setChatHistory(prev => [...prev, { role: "assistant", content: reply }]);
      setActiveCitations(citations);
      setIsChatLoading(false);
    }, 1200);
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 font-sans">

      {/* Top Header */}
      <nav className="bg-slate-950 border-b border-slate-800 sticky top-0 z-50 px-6 py-4 shadow-xl">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between">
          <div className="flex items-center space-x-3">
            <Layers className="h-8 w-8 text-blue-500 animate-pulse" />
            <div>
              <input
                type="text"
                value={appName}
                onChange={(e) => setAppName(e.target.value)}
                className="bg-transparent font-black text-xl text-white outline-none border-b border-transparent focus:border-blue-500 transition duration-200 w-full"
              />
              <p className="text-xs text-slate-400">Automated SDLC Compliance & Local RAG Environment</p>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-4 mt-4 md:mt-0 text-sm">
            <span className="bg-blue-900/40 text-blue-300 border border-blue-800/60 px-3 py-1.5 rounded-full font-bold">
              App Workspace Code: {appCode}
            </span>
            <div className="flex bg-slate-800/80 rounded-lg p-1 border border-slate-700">
              <button
                onClick={() => setRole("MAKER")}
                className={`px-3 py-1 rounded text-xs font-bold transition ${role === 'MAKER' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}
              >
                MAKER
              </button>
              <button
                onClick={() => setRole("CHECKER")}
                className={`px-3 py-1 rounded text-xs font-bold transition ${role === 'CHECKER' ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'}`}
              >
                CHECKER (APPROVER)
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Grid Workspace */}
      <main className="max-w-7xl mx-auto px-6 py-8 grid grid-cols-1 lg:grid-cols-12 gap-8">

        {/* Left Column: Toggles and 7-Phases Dashboard */}
        <div className="lg:col-span-8 space-y-8">

          {/* Status alerts */}
          {successMsg && (
            <div className="bg-emerald-900/60 border border-emerald-500/50 text-emerald-200 p-4 rounded-xl shadow-lg flex items-center space-x-3">
              <CheckCircle className="h-5 w-5 text-emerald-400 flex-shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}
          {errorMsg && (
            <div className="bg-red-900/60 border border-red-500/50 text-red-200 p-4 rounded-xl shadow-lg flex items-center space-x-3">
              <AlertTriangle className="h-5 w-5 text-red-400 flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Quick Config panel */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <h3 className="text-sm font-black text-slate-400 uppercase tracking-wider mb-3 flex items-center">
                <Sliders className="h-4 w-4 mr-1 text-blue-500" />
                Auth Configuration
              </h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <span>LDAP Active Directory Mode</span>
                  <button
                    onClick={() => setAuthStrategy(authStrategy === "DB" ? "LDAP" : "DB")}
                    className="text-blue-400 hover:text-blue-300 font-bold"
                  >
                    {authStrategy === 'DB' ? 'OFF (Local DB)' : 'ON (LDAP AD)'}
                  </button>
                </div>
                <div className="flex items-center justify-between text-xs">
                  <span>MFA (SMS/Email Switched)</span>
                  <button
                    onClick={() => setMfaEnabled(!mfaEnabled)}
                    className="text-blue-400 hover:text-blue-300 font-bold"
                  >
                    {mfaEnabled ? 'ENABLED' : 'DISABLED'}
                  </button>
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-sm font-black text-slate-400 uppercase tracking-wider mb-3 flex items-center">
                <ShieldAlert className="h-4 w-4 mr-1 text-blue-500" />
                Workflow Alerts
              </h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between text-xs">
                  <span>Alert Approvers on Drop</span>
                  <button
                    onClick={() => setAlertsEnabled(!alertsEnabled)}
                    className="text-blue-400 hover:text-blue-300 font-bold"
                  >
                    {alertsEnabled ? 'ENABLED' : 'DISABLED'}
                  </button>
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-sm font-black text-slate-400 uppercase tracking-wider mb-3 flex items-center">
                <FolderCheck className="h-4 w-4 mr-1 text-blue-500" />
                Auto-Poller Drops
              </h3>
              <button
                onClick={handleDropSimulation}
                className="w-full bg-slate-800 hover:bg-slate-700 border border-slate-700 text-xs font-bold py-2 rounded-lg transition"
              >
                Simulate Dropped File
              </button>
            </div>
          </div>

          {/* Interactive Phase Deliverables Grid */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-black text-white">7-Phase Deliverables Lifecycle</h2>
                <p className="text-xs text-slate-400 mt-1">Tagging sequences automatically from P001 to P016</p>
              </div>
              <span className="text-xs bg-blue-900/30 text-blue-400 px-3 py-1 rounded-full border border-blue-800">7 SDLC Phases</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {currentDeliverables.map((phase) => (
                <div key={phase.num} className="bg-slate-900/50 p-4 rounded-xl border border-slate-800 flex flex-col justify-between">
                  <div>
                    <span className="text-[10px] font-black uppercase text-blue-500 tracking-wider">Phase {phase.num}</span>
                    <h3 className="text-sm font-bold text-white mt-1 mb-3">{phase.name}</h3>

                    <div className="space-y-2">
                      {phase.deliverables.map(doc => (
                        <div
                          key={doc.id}
                          onClick={() => handleSelectDoc(doc)}
                          className={`p-2 rounded-lg text-xs cursor-pointer border transition flex items-center justify-between ${selectedDoc.id === doc.id ? 'bg-blue-950/60 border-blue-500 text-white font-bold' : 'bg-slate-950/40 border-slate-800/80 text-slate-300 hover:bg-slate-800/40'}`}
                        >
                          <div>
                            <span className="font-mono text-blue-400 mr-2">{doc.id}</span>
                            <span>{doc.title}</span>
                          </div>
                          <ChevronRight className="h-3 w-3 text-slate-400" />
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Maker upload form */}
          {role === 'MAKER' && (
            <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl">
              <h3 className="text-lg font-bold text-white mb-4">Manual Upload Deliverable Document</h3>
              <form onSubmit={handleSubmitFile} className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase mb-1">Target Deliverable</label>
                  <select
                    value={selectedDoc.id}
                    onChange={(e) => {
                      const allDocs = PHASES.flatMap(p => p.deliverables);
                      const matched = allDocs.find(d => d.id === e.target.value);
                      if (matched) handleSelectDoc(matched);
                    }}
                    className="w-full bg-slate-900 border border-slate-800 rounded-lg p-2 text-xs text-white"
                  >
                    {PHASES.flatMap(p => p.deliverables).map(d => (
                      <option key={d.id} value={d.id}>{d.id} - {d.title}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase mb-1">Version Number</label>
                  <input
                    type="text"
                    value={version}
                    onChange={(e) => setVersion(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-lg p-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase mb-1">Document Code</label>
                  <input
                    type="text"
                    value={docCode}
                    onChange={(e) => setDocCode(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-lg p-2 text-xs text-white"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase mb-1">Choose File</label>
                  <input
                    type="file"
                    onChange={(e) => setUploadFile(e.target.files[0])}
                    className="w-full text-xs text-slate-400"
                  />
                </div>
                <div className="md:col-span-4 text-right">
                  <button
                    type="submit"
                    className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-5 rounded-lg text-xs shadow-md transition"
                  >
                    Upload and Trigger Local AI Pipelines
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Submissions queue and maker checker reviews */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl">
            <h3 className="text-lg font-bold text-white mb-4">Ingested Document Catalog & Review Logs</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 font-black uppercase tracking-wider">
                    <th className="py-2">Deliverable ID</th>
                    <th className="py-2">File Name</th>
                    <th className="py-2">Version/Code</th>
                    <th className="py-2">Status</th>
                    <th className="py-2">Submitter</th>
                    <th className="py-2 text-center">Maker-Checker Approvals</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {submissions.map((sub) => (
                    <tr key={sub.id} className="hover:bg-slate-900/40">
                      <td className="py-3 font-mono font-bold text-blue-400">{sub.docId}</td>
                      <td className="py-3 font-medium text-slate-100">{sub.fileName}</td>
                      <td className="py-3 text-slate-400">{sub.version} | {sub.code}</td>
                      <td className="py-3">
                        <span className={`px-2 py-0.5 rounded font-black text-[9px] ${sub.status === 'APPROVED' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : sub.status === 'REJECTED' ? 'bg-red-950 text-red-400 border border-red-800' : 'bg-amber-950 text-amber-400 border border-amber-800'}`}>
                          {sub.status}
                        </span>
                      </td>
                      <td className="py-3 text-slate-400">{sub.maker}</td>
                      <td className="py-3 text-center">
                        {role === 'CHECKER' && sub.status === 'PENDING_APPROVAL' ? (
                          <div className="flex items-center justify-center space-x-2">
                            <button
                              onClick={() => handleApprove(sub.id)}
                              className="bg-emerald-600 hover:bg-emerald-700 text-white px-2 py-1 rounded text-[10px] font-bold"
                            >
                              Approve
                            </button>
                            <button
                              onClick={() => handleReject(sub.id)}
                              className="bg-red-600 hover:bg-red-700 text-white px-2 py-1 rounded text-[10px] font-bold"
                            >
                              Reject
                            </button>
                          </div>
                        ) : (
                          <span className="text-slate-500 italic">{sub.checker ? `Approved by ${sub.checker}` : 'Awaiting Review'}</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Right Column: Local AI Document Recommendations Side-By-Side & Chat */}
        <div className="lg:col-span-4 space-y-8">

          {/* Side-by-Side Document AI Analytics Panel */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
              <h3 className="font-black text-white text-md flex items-center">
                <Sparkles className="h-5 w-5 text-blue-400 mr-2" />
                AI Document Advisor
              </h3>
              <span className="text-[10px] bg-slate-900 text-slate-400 font-mono font-bold px-2 py-0.5 rounded border border-slate-800">
                {selectedDoc.id}
              </span>
            </div>

            {isAnalyzing ? (
              <div className="py-12 text-center space-y-3">
                <RefreshCw className="h-8 w-8 text-blue-500 animate-spin mx-auto" />
                <p className="text-xs text-slate-400">Performing local layout extraction & FAISS similarity indexing...</p>
              </div>
            ) : (
              <div className="space-y-5 text-xs">

                {/* Deliverable Metadata header */}
                <div className="bg-slate-900 p-3 rounded-lg border border-slate-800">
                  <h4 className="font-bold text-white text-xs">{selectedDoc.title}</h4>
                  <p className="text-slate-400 italic mt-0.5">{selectedDoc.desc}</p>
                </div>

                {/* 1. Summary */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">1. Summary</h5>
                  <p className="text-slate-400 leading-relaxed bg-slate-900/30 p-2 rounded border border-slate-850">{aiReport.summary}</p>
                </div>

                {/* 2. Expected/Missing Sections */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">2. Expected vs Missing Sections</h5>
                  <div className="flex flex-wrap gap-1.5 mt-1.5">
                    {aiReport.missing_sections.map((sec, idx) => (
                      <span key={idx} className="bg-red-950/40 text-red-400 px-2 py-0.5 rounded text-[10px] font-bold border border-red-900/50 flex items-center">
                        <AlertTriangle className="h-3 w-3 mr-1" />
                        Missing: {sec}
                      </span>
                    ))}
                    {aiReport.missing_sections.length === 0 && (
                      <span className="bg-emerald-950/40 text-emerald-400 px-2 py-0.5 rounded text-[10px] font-bold border border-emerald-900/50">
                        100% Sections Match
                      </span>
                    )}
                  </div>
                </div>

                {/* 3 & 6. Scores */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">3. Compliance Score</h5>
                    <div className="flex items-center space-x-2">
                      <div className="w-full bg-slate-800 rounded-full h-1.5">
                        <div className="bg-blue-500 h-1.5 rounded-full" style={{ width: `${aiReport.compliance_score}%` }}></div>
                      </div>
                      <span className="font-bold font-mono text-white">{aiReport.compliance_score}%</span>
                    </div>
                  </div>
                  <div>
                    <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">6. Quality Score</h5>
                    <div className="flex items-center space-x-2">
                      <div className="w-full bg-slate-800 rounded-full h-1.5">
                        <div className="bg-emerald-500 h-1.5 rounded-full" style={{ width: `${aiReport.quality_score}%` }}></div>
                      </div>
                      <span className="font-bold font-mono text-white">{aiReport.quality_score}%</span>
                    </div>
                  </div>
                </div>

                {/* 4. Suggested Improvements */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">4. Suggested Improvements</h5>
                  <ul className="list-disc list-inside space-y-1 text-slate-400 pl-1">
                    {aiReport.suggested_improvements.map((imp, idx) => (
                      <li key={idx}>{imp}</li>
                    ))}
                  </ul>
                </div>

                {/* 5. Risk Assessment */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">5. Risk Assessment</h5>
                  <div className="bg-slate-900/30 p-2 rounded border border-slate-850 flex items-start space-x-2">
                    <span className={`px-1.5 py-0.5 rounded text-[9px] font-black ${aiReport.risk_assessment.includes("HIGH") ? 'bg-red-950 text-red-400 border border-red-800' : 'bg-amber-950 text-amber-400 border border-amber-800'}`}>
                      {aiReport.risk_assessment.includes("HIGH") ? 'HIGH' : 'MEDIUM'}
                    </span>
                    <span className="text-slate-400">{aiReport.risk_assessment}</span>
                  </div>
                </div>

                {/* 7. Similar Documents */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">7. Similar Documents</h5>
                  <div className="space-y-1">
                    {aiReport.similar_documents.map((doc, idx) => (
                      <div key={idx} className="bg-slate-900 p-2 rounded border border-slate-800 text-[10px] text-blue-400 font-bold flex items-center justify-between">
                        <span>{doc}</span>
                        <span className="text-slate-500 font-mono text-[9px]">FAISS Cosine Similarity: 0.89</span>
                      </div>
                    ))}
                    {aiReport.similar_documents.length === 0 && (
                      <p className="text-slate-500 italic">No similar deliverables registered.</p>
                    )}
                  </div>
                </div>

                {/* 8. Version Comparison */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">8. Version Comparison</h5>
                  <p className="text-slate-400 leading-relaxed bg-slate-900/30 p-2 rounded border border-slate-850">{aiReport.version_comparison}</p>
                </div>

                {/* 9. Duplicate Detection */}
                <div>
                  <h5 className="font-bold text-slate-300 uppercase tracking-wider mb-1">9. Duplicate Detection</h5>
                  <div className="p-2 rounded bg-slate-900/40 border border-slate-800 text-slate-400 flex items-center space-x-2">
                    <div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse"></div>
                    <span>{aiReport.duplicate_detection}</span>
                  </div>
                </div>

              </div>
            )}
          </div>

          {/* Local RAG Streaming Chat Interface */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl flex flex-col h-[400px]">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
              <h3 className="font-black text-white text-md flex items-center">
                <MessageSquare className="h-5 w-5 text-blue-400 mr-2" />
                Local RAG Chat (Ollama)
              </h3>
              <span className="bg-emerald-950 text-emerald-400 px-2 py-0.5 rounded text-[10px] font-black border border-emerald-800 flex items-center">
                llama3.2
              </span>
            </div>

            {/* Conversation Window */}
            <div className="flex-1 overflow-y-auto space-y-3 mb-4 text-xs pr-1">
              {chatHistory.map((chat, idx) => (
                <div key={idx} className={`p-2.5 rounded-xl max-w-[85%] ${chat.role === 'user' ? 'bg-blue-600/90 text-white ml-auto' : 'bg-slate-900 border border-slate-800 text-slate-300'}`}>
                  <p className="leading-relaxed">{chat.content}</p>
                </div>
              ))}
              {isChatLoading && (
                <div className="bg-slate-900 border border-slate-800 p-2.5 rounded-xl max-w-[40%] flex items-center space-x-2 text-slate-400">
                  <RefreshCw className="h-3 w-3 animate-spin text-blue-400" />
                  <span>Streaming from llama3.2...</span>
                </div>
              )}
            </div>

            {/* Citations Box */}
            {activeCitations.length > 0 && (
              <div className="bg-slate-900 p-2 rounded-lg border border-slate-800 text-[10px] mb-3 text-slate-400">
                <span className="font-bold text-slate-300 uppercase block mb-1">Citations & Source References:</span>
                {activeCitations.map((cit, i) => (
                  <div key={i} className="flex items-center justify-between mt-0.5">
                    <span className="text-blue-400 font-bold">{cit.file_name}</span>
                    <span className="font-mono bg-slate-950 px-1 rounded text-slate-500">{cit.section}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Chat submit bar */}
            <form onSubmit={handleChatSubmit} className="flex items-center space-x-2">
              <input
                type="text"
                placeholder="Ask about deliverables or search FAISS..."
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                className="flex-1 bg-slate-900 border border-slate-800 rounded-lg px-3 py-2 text-xs text-white outline-none focus:border-blue-500"
              />
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white p-2 rounded-lg transition"
              >
                <Send className="h-4 w-4" />
              </button>
            </form>
          </div>

          {/* Poller log view */}
          <div className="bg-slate-950/80 rounded-2xl border border-slate-800 p-6 shadow-xl text-xs font-mono h-[200px] overflow-hidden flex flex-col">
            <h4 className="font-bold text-slate-400 border-b border-slate-800 pb-2 mb-2 uppercase">Background Auto-Poller Logs</h4>
            <div className="flex-1 overflow-y-auto space-y-1.5 text-[10px] text-emerald-500">
              {pollerLog.map((log, idx) => (
                <p key={idx}>{log}</p>
              ))}
            </div>
          </div>

        </div>

      </main>
    </div>
  );
}

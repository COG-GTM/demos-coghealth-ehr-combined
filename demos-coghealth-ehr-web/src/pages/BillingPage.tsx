import { useState } from 'react';
import {
  RefreshCw,
  Printer,
  Download,
  ChevronDown,
  ChevronRight,
  FileText,
  RotateCcw,
  AlertCircle,
} from 'lucide-react';
import { AlertDialog } from '../components/ui/Modal';
import { PrintDialog } from '../components/ui/PrintDialog';
import type { Claim, ClaimStatus, ClaimCategory } from '../types/billing';

const defaultClaims: Claim[] = [
  {
    id: 'CLM-2024-0001', claimNumber: 'CLM-2024-0001', patientId: 1,
    patientName: 'Smith, John', patientMrn: 'MRN001234',
    payer: 'Blue Cross PPO', payerId: 'BCBS01',
    cptCodes: ['99213', '93000'], icdCodes: ['E11.9', 'I10'],
    billedAmount: 425.00, allowedAmount: 310.00, paidAmount: 248.00, balance: 62.00,
    status: 'PARTIAL', category: 'active',
    dateOfService: '01/15/2024', dateSubmitted: '01/16/2024', datePaid: '02/01/2024',
    provider: 'Dr. Anderson', agingDays: 18, authorizationNumber: 'AUTH-88123',
  },
  {
    id: 'CLM-2024-0002', claimNumber: 'CLM-2024-0002', patientId: 2,
    patientName: 'Johnson, Sarah', patientMrn: 'MRN001235',
    payer: 'Aetna HMO', payerId: 'AET01',
    cptCodes: ['99214'], icdCodes: ['J45.20'],
    billedAmount: 285.00, allowedAmount: undefined, paidAmount: undefined, balance: 285.00,
    status: 'SUBMITTED', category: 'active',
    dateOfService: '01/17/2024', dateSubmitted: '01/18/2024',
    provider: 'Dr. Anderson', agingDays: 7,
  },
  {
    id: 'CLM-2024-0003', claimNumber: 'CLM-2024-0003', patientId: 3,
    patientName: 'Williams, Michael', patientMrn: 'MRN001236',
    payer: 'Medicare Part B', payerId: 'MCR01',
    cptCodes: ['99215', '36415'], icdCodes: ['Z00.00'],
    billedAmount: 520.00, allowedAmount: undefined, paidAmount: undefined, balance: 520.00,
    status: 'PENDING', category: 'active',
    dateOfService: '01/10/2024', dateSubmitted: '01/11/2024',
    provider: 'Dr. Anderson', agingDays: 14, authorizationNumber: 'MCR-99341',
  },
  {
    id: 'CLM-2024-0004', claimNumber: 'CLM-2024-0004', patientId: 4,
    patientName: 'Brown, Emily', patientMrn: 'MRN001237',
    payer: 'UnitedHealthcare', payerId: 'UHC01',
    cptCodes: ['99212'], icdCodes: ['K21.0'],
    billedAmount: 195.00, allowedAmount: undefined, paidAmount: undefined, balance: 195.00,
    status: 'DENIED', category: 'denied',
    dateOfService: '01/08/2024', dateSubmitted: '01/09/2024',
    denialReason: 'CO-4: Service not covered under current plan',
    provider: 'Dr. Anderson', agingDays: 36,
  },
  {
    id: 'CLM-2024-0005', claimNumber: 'CLM-2024-0005', patientId: 5,
    patientName: 'Davis, Robert', patientMrn: 'MRN001238',
    payer: 'Medicare Part B', payerId: 'MCR01',
    cptCodes: ['99213', '93306'], icdCodes: ['I25.10', 'I10'],
    billedAmount: 890.00, allowedAmount: 640.00, paidAmount: 512.00, balance: 0,
    status: 'PAID', category: 'paid',
    dateOfService: '12/20/2023', dateSubmitted: '12/21/2023', datePaid: '01/12/2024',
    provider: 'Dr. Anderson', agingDays: 0,
  },
  {
    id: 'CLM-2024-0006', claimNumber: 'CLM-2024-0006', patientId: 6,
    patientName: 'Martinez, Maria', patientMrn: 'MRN001240',
    payer: 'Cigna PPO', payerId: 'CIG01',
    cptCodes: ['99214', '85025'], icdCodes: ['D64.9'],
    billedAmount: 360.00, allowedAmount: undefined, paidAmount: undefined, balance: 360.00,
    status: 'APPEALED', category: 'denied',
    dateOfService: '12/28/2023', dateSubmitted: '12/29/2023',
    denialReason: 'CO-11: Diagnosis inconsistent with procedure',
    provider: 'Dr. Anderson', agingDays: 47,
  },
  {
    id: 'CLM-2024-0007', claimNumber: 'CLM-2024-0007', patientId: 1,
    patientName: 'Smith, John', patientMrn: 'MRN001234',
    payer: 'Blue Cross PPO', payerId: 'BCBS01',
    cptCodes: ['99213'], icdCodes: ['E11.9'],
    billedAmount: 210.00, allowedAmount: 175.00, paidAmount: 175.00, balance: 0,
    status: 'PAID', category: 'paid',
    dateOfService: '11/15/2023', dateSubmitted: '11/16/2023', datePaid: '12/05/2023',
    provider: 'Dr. Anderson', agingDays: 0,
  },
  {
    id: 'CLM-2024-0008', claimNumber: 'CLM-2024-0008', patientId: 3,
    patientName: 'Williams, Michael', patientMrn: 'MRN001236',
    payer: 'Medicare Part B', payerId: 'MCR01',
    cptCodes: ['99215', '71046'], icdCodes: ['J18.9'],
    billedAmount: 680.00, allowedAmount: undefined, paidAmount: undefined, balance: 680.00,
    status: 'DENIED', category: 'denied',
    dateOfService: '01/03/2024', dateSubmitted: '01/04/2024',
    denialReason: 'CO-50: Non-covered service – not deemed medically necessary',
    provider: 'Dr. Anderson', agingDays: 62,
  },
  {
    id: 'CLM-2024-0009', claimNumber: 'CLM-2024-0009', patientId: 2,
    patientName: 'Johnson, Sarah', patientMrn: 'MRN001235',
    payer: 'Aetna HMO', payerId: 'AET01',
    cptCodes: ['99213', '94640'], icdCodes: ['J45.41'],
    billedAmount: 415.00, allowedAmount: 300.00, paidAmount: 300.00, balance: 0,
    status: 'PAID', category: 'paid',
    dateOfService: '11/30/2023', dateSubmitted: '12/01/2023', datePaid: '12/22/2023',
    provider: 'Dr. Anderson', agingDays: 0,
  },
  {
    id: 'CLM-2024-0010', claimNumber: 'CLM-2024-0010', patientId: 5,
    patientName: 'Davis, Robert', patientMrn: 'MRN001238',
    payer: 'Medicare Part B', payerId: 'MCR01',
    cptCodes: ['99214'], icdCodes: ['I25.10'],
    billedAmount: 290.00, allowedAmount: undefined, paidAmount: undefined, balance: 290.00,
    status: 'SUBMITTED', category: 'active',
    dateOfService: '01/16/2024', dateSubmitted: '01/17/2024',
    provider: 'Dr. Anderson', agingDays: 8,
  },
  {
    id: 'CLM-2024-0011', claimNumber: 'CLM-2024-0011', patientId: 6,
    patientName: 'Martinez, Maria', patientMrn: 'MRN001240',
    payer: 'Cigna PPO', payerId: 'CIG01',
    cptCodes: ['99212', '85652'], icdCodes: ['D64.9', 'R53.83'],
    billedAmount: 245.00, allowedAmount: 190.00, paidAmount: 190.00, balance: 0,
    status: 'PAID', category: 'paid',
    dateOfService: '11/20/2023', dateSubmitted: '11/21/2023', datePaid: '12/10/2023',
    provider: 'Dr. Anderson', agingDays: 0,
  },
  {
    id: 'CLM-2024-0012', claimNumber: 'CLM-2024-0012', patientId: 4,
    patientName: 'Brown, Emily', patientMrn: 'MRN001237',
    payer: 'UnitedHealthcare', payerId: 'UHC01',
    cptCodes: ['99214', '43239'], icdCodes: ['K21.0', 'K57.30'],
    billedAmount: 1240.00, allowedAmount: undefined, paidAmount: undefined, balance: 1240.00,
    status: 'PENDING', category: 'active',
    dateOfService: '01/05/2024', dateSubmitted: '01/06/2024',
    provider: 'Dr. Anderson', agingDays: 19, authorizationNumber: 'UHC-55821',
  },
];

const fmt = (n: number) =>
  n.toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const statusConfig: Record<ClaimStatus, { label: string; bg: string; color: string }> = {
  SUBMITTED: { label: 'Submitted', bg: '#cce5ff', color: '#004085' },
  PENDING:   { label: 'Pending',   bg: '#fff3cd', color: '#664d00' },
  DENIED:    { label: 'Denied',    bg: '#f8d7da', color: '#721c24' },
  PAID:      { label: 'Paid',      bg: '#d4edda', color: '#155724' },
  APPEALED:  { label: 'Appealed',  bg: '#e2d9f3', color: '#432874' },
  PARTIAL:   { label: 'Partial',   bg: '#fde8d1', color: '#7d3c00' },
};

type StatusFilter = 'ALL' | ClaimStatus;

const categoryLabels: Record<ClaimCategory, string> = {
  active: 'Active Claims',
  denied: 'Denied / Appealed Claims',
  paid: 'Paid Claims',
};

export default function BillingPage() {
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const [expandedCategories, setExpandedCategories] = useState<Set<ClaimCategory>>(
    new Set(['active', 'denied', 'paid'])
  );
  const [showPrintDialog, setShowPrintDialog] = useState(false);
  const [alert, setAlert] = useState<{ title: string; message: string; type: 'success' | 'info' | 'warning' } | null>(null);

  const toggleCategory = (cat: ClaimCategory) => {
    const next = new Set(expandedCategories);
    if (next.has(cat)) next.delete(cat);
    else next.add(cat);
    setExpandedCategories(next);
  };

  const filtered = defaultClaims.filter(
    c => statusFilter === 'ALL' || c.status === statusFilter
  );

  const byCategory = filtered.reduce((acc, c) => {
    if (!acc[c.category]) acc[c.category] = [];
    acc[c.category].push(c);
    return acc;
  }, {} as Record<ClaimCategory, Claim[]>);

  // Sidebar derived data
  const totalBilled = defaultClaims.reduce((s, c) => s + c.billedAmount, 0);
  const totalCollected = defaultClaims.reduce((s, c) => s + (c.paidAmount ?? 0), 0);
  const totalPending = defaultClaims.filter(c => c.category === 'active').reduce((s, c) => s + c.balance, 0);
  const pendingCount = defaultClaims.filter(c => c.category === 'active').length;
  const deniedCount = defaultClaims.filter(c => c.status === 'DENIED' || c.status === 'APPEALED').length;
  const denialRate = Math.round((deniedCount / defaultClaims.length) * 100);

  const payerTotals = defaultClaims.reduce((acc, c) => {
    if (!acc[c.payer]) acc[c.payer] = { count: 0, billed: 0 };
    acc[c.payer].count += 1;
    acc[c.payer].billed += c.billedAmount;
    return acc;
  }, {} as Record<string, { count: number; billed: number }>);

  const agingBuckets = [
    { label: '0–30 days',  amount: defaultClaims.filter(c => c.agingDays <= 30  && c.balance > 0).reduce((s, c) => s + c.balance, 0), count: defaultClaims.filter(c => c.agingDays <= 30  && c.balance > 0).length },
    { label: '31–60 days', amount: defaultClaims.filter(c => c.agingDays > 30 && c.agingDays <= 60 && c.balance > 0).reduce((s, c) => s + c.balance, 0), count: defaultClaims.filter(c => c.agingDays > 30 && c.agingDays <= 60 && c.balance > 0).length },
    { label: '61–90 days', amount: defaultClaims.filter(c => c.agingDays > 60 && c.agingDays <= 90 && c.balance > 0).reduce((s, c) => s + c.balance, 0), count: defaultClaims.filter(c => c.agingDays > 60 && c.agingDays <= 90 && c.balance > 0).length },
    { label: '90+ days',   amount: defaultClaims.filter(c => c.agingDays > 90  && c.balance > 0).reduce((s, c) => s + c.balance, 0), count: defaultClaims.filter(c => c.agingDays > 90  && c.balance > 0).length },
  ];

  const categoryOrder: ClaimCategory[] = ['active', 'denied', 'paid'];

  return (
    <div className="h-full flex flex-col" style={{ background: '#d4d0c8' }}>
      {/* Toolbar */}
      <div className="ehr-toolbar flex items-center justify-between">
        <div className="flex items-center space-x-1">
          <button
            className="ehr-toolbar-button flex items-center"
            onClick={() => setAlert({ title: 'Refreshed', message: 'Billing data has been refreshed.', type: 'info' })}
          >
            <RefreshCw className="w-3.5 h-3.5 mr-1" /> Refresh
          </button>
          <span className="text-gray-400">|</span>
          <button
            className="ehr-toolbar-button flex items-center"
            onClick={() => setShowPrintDialog(true)}
          >
            <Printer className="w-3.5 h-3.5 mr-1" /> Print
          </button>
          <button
            className="ehr-toolbar-button flex items-center"
            onClick={() => setAlert({ title: 'Export', message: 'Claims data exported to CSV.', type: 'success' })}
          >
            <Download className="w-3.5 h-3.5 mr-1" /> Export
          </button>
        </div>
        <div className="flex items-center space-x-2">
          <span className="text-gray-600 text-[10px]">Status:</span>
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value as StatusFilter)}
            className="ehr-input"
          >
            <option value="ALL">All Statuses</option>
            <option value="SUBMITTED">Submitted</option>
            <option value="PENDING">Pending</option>
            <option value="DENIED">Denied</option>
            <option value="PAID">Paid</option>
            <option value="APPEALED">Appealed</option>
            <option value="PARTIAL">Partial</option>
          </select>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Panel */}
        <div className="w-56 overflow-auto p-2 space-y-2 shrink-0" style={{ background: '#ece9d8' }}>
          {/* Billing Summary */}
          <fieldset className="ehr-fieldset">
            <legend>Billing Summary</legend>
            <table className="w-full text-[10px]">
              <tbody>
                <tr className="bg-white">
                  <td className="px-1 py-0.5 border border-gray-400">Total Billed</td>
                  <td className="px-1 py-0.5 border border-gray-400 text-right font-bold">{fmt(totalBilled)}</td>
                </tr>
                <tr>
                  <td className="px-1 py-0.5 border border-gray-400">Total Collected</td>
                  <td className="px-1 py-0.5 border border-gray-400 text-right font-bold text-green-700">{fmt(totalCollected)}</td>
                </tr>
                <tr className="bg-white">
                  <td className="px-1 py-0.5 border border-gray-400">Pending Balance</td>
                  <td className="px-1 py-0.5 border border-gray-400 text-right font-bold text-orange-700">{fmt(totalPending)}</td>
                </tr>
                <tr>
                  <td className="px-1 py-0.5 border border-gray-400">Open Claims</td>
                  <td className="px-1 py-0.5 border border-gray-400 text-right font-bold">{pendingCount}</td>
                </tr>
                <tr className="bg-white">
                  <td className="px-1 py-0.5 border border-gray-400">Denial Rate</td>
                  <td className={`px-1 py-0.5 border border-gray-400 text-right font-bold ${denialRate > 10 ? 'text-red-700' : 'text-green-700'}`}>{denialRate}%</td>
                </tr>
              </tbody>
            </table>
          </fieldset>

          {/* Claims by Payer */}
          <fieldset className="ehr-fieldset">
            <legend>Claims by Payer</legend>
            <table className="w-full text-[10px]">
              <thead>
                <tr>
                  <th className="px-1 py-0.5 text-left border border-gray-400 bg-gray-100">Payer</th>
                  <th className="px-1 py-0.5 text-right border border-gray-400 bg-gray-100">#</th>
                  <th className="px-1 py-0.5 text-right border border-gray-400 bg-gray-100">Billed</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(payerTotals).map(([payer, data], idx) => (
                  <tr key={payer} className={idx % 2 === 0 ? 'bg-white' : ''}>
                    <td className="px-1 py-0.5 border border-gray-400 truncate max-w-[70px]" title={payer}>{payer}</td>
                    <td className="px-1 py-0.5 border border-gray-400 text-right">{data.count}</td>
                    <td className="px-1 py-0.5 border border-gray-400 text-right">{fmt(data.billed)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </fieldset>

          {/* Aging Buckets */}
          <fieldset className="ehr-fieldset">
            <legend>Aging (A/R)</legend>
            <table className="w-full text-[10px]">
              <thead>
                <tr>
                  <th className="px-1 py-0.5 text-left border border-gray-400 bg-gray-100">Period</th>
                  <th className="px-1 py-0.5 text-right border border-gray-400 bg-gray-100">Amt</th>
                </tr>
              </thead>
              <tbody>
                {agingBuckets.map((b, idx) => (
                  <tr key={b.label} className={idx % 2 === 0 ? 'bg-white' : ''}>
                    <td className="px-1 py-0.5 border border-gray-400">{b.label}</td>
                    <td className={`px-1 py-0.5 border border-gray-400 text-right font-bold ${b.amount > 500 ? 'text-red-700' : ''}`}>
                      {b.amount > 0 ? fmt(b.amount) : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </fieldset>
        </div>

        {/* Right Panel */}
        <div className="flex-1 overflow-auto bg-white border-l border-gray-500">
          {categoryOrder.map(cat => {
            const claims = byCategory[cat];
            if (!claims || claims.length === 0) return null;
            const isExpanded = expandedCategories.has(cat);
            return (
              <div key={cat} className="border-b border-gray-300">
                {/* Category Header */}
                <div
                  onClick={() => toggleCategory(cat)}
                  className="px-2 py-1 cursor-pointer flex items-center justify-between text-[11px] border-b border-gray-400"
                  style={{ background: 'linear-gradient(to bottom, #f8f8f8 0%, #e0e0e0 100%)' }}
                >
                  <div className="flex items-center space-x-2">
                    <span className="w-4 h-4 border border-gray-500 bg-white flex items-center justify-center text-[10px] font-bold">
                      {isExpanded ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
                    </span>
                    <span className="font-semibold">{categoryLabels[cat]}</span>
                    <span className="text-[9px] text-gray-600">({claims.length})</span>
                    {cat === 'denied' && (
                      <span className="flex items-center text-red-600 text-[9px]">
                        <AlertCircle className="w-3 h-3 mr-0.5" /> Requires attention
                      </span>
                    )}
                  </div>
                  <span className="text-[10px] text-gray-600 font-semibold">
                    Total: {fmt(claims.reduce((s, c) => s + c.billedAmount, 0))}
                  </span>
                </div>

                {/* Claims Table */}
                {isExpanded && (
                  <table className="w-full text-[10px]">
                    <thead>
                      <tr>
                        <th className="px-2 py-1 text-left w-32">Claim #</th>
                        <th className="px-2 py-1 text-left">Patient</th>
                        <th className="px-2 py-1 text-left w-28">Payer</th>
                        <th className="px-2 py-1 text-left w-20">CPT Codes</th>
                        <th className="px-2 py-1 text-right w-20">Billed</th>
                        <th className="px-2 py-1 text-right w-20">Allowed</th>
                        <th className="px-2 py-1 text-right w-20">Paid</th>
                        <th className="px-2 py-1 text-right w-16">Balance</th>
                        <th className="px-2 py-1 text-center w-20">Status</th>
                        <th className="px-2 py-1 text-left w-20">DOS</th>
                        <th className="px-2 py-1 text-center w-28">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {claims.map((claim, idx) => {
                        const sc = statusConfig[claim.status];
                        return (
                          <tr key={claim.id} className={`hover:bg-blue-50 ${idx % 2 === 1 ? 'bg-gray-50' : 'bg-white'}`}>
                            <td className="px-2 py-1.5 font-mono text-[9px] text-blue-700">{claim.claimNumber}</td>
                            <td className="px-2 py-1.5">
                              <div className="font-semibold">{claim.patientName}</div>
                              <div className="text-[9px] text-gray-500">{claim.patientMrn}</div>
                            </td>
                            <td className="px-2 py-1.5 text-[9px]">{claim.payer}</td>
                            <td className="px-2 py-1.5 font-mono text-[9px]">{claim.cptCodes.join(', ')}</td>
                            <td className="px-2 py-1.5 text-right">{fmt(claim.billedAmount)}</td>
                            <td className="px-2 py-1.5 text-right text-gray-500">
                              {claim.allowedAmount != null ? fmt(claim.allowedAmount) : '—'}
                            </td>
                            <td className="px-2 py-1.5 text-right text-green-700">
                              {claim.paidAmount != null ? fmt(claim.paidAmount) : '—'}
                            </td>
                            <td className={`px-2 py-1.5 text-right font-semibold ${claim.balance > 0 ? 'text-orange-700' : 'text-gray-400'}`}>
                              {claim.balance > 0 ? fmt(claim.balance) : '—'}
                            </td>
                            <td className="px-2 py-1.5 text-center">
                              <span
                                className="px-1.5 py-0.5 text-[9px] font-semibold border"
                                style={{ background: sc.bg, color: sc.color, borderColor: sc.color + '55' }}
                              >
                                {sc.label}
                              </span>
                            </td>
                            <td className="px-2 py-1.5 text-[9px]">
                              <div>{claim.dateOfService}</div>
                              {claim.denialReason && (
                                <div className="text-red-600 text-[8px] mt-0.5 truncate max-w-[80px]" title={claim.denialReason}>
                                  {claim.denialReason.substring(0, 20)}…
                                </div>
                              )}
                            </td>
                            <td className="px-2 py-1.5 text-center space-x-1">
                              <button
                                className="ehr-button text-[9px] px-1.5 py-0.5"
                                onClick={() => setAlert({
                                  title: `Claim ${claim.claimNumber}`,
                                  message: `Patient: ${claim.patientName} (${claim.patientMrn})\nPayer: ${claim.payer}\nCPT: ${claim.cptCodes.join(', ')}\nICD: ${claim.icdCodes.join(', ')}\nBilled: ${fmt(claim.billedAmount)}\nAllowed: ${claim.allowedAmount != null ? fmt(claim.allowedAmount) : 'N/A'}\nPaid: ${claim.paidAmount != null ? fmt(claim.paidAmount) : 'N/A'}\nBalance: ${fmt(claim.balance)}\nDate of Service: ${claim.dateOfService}\nSubmitted: ${claim.dateSubmitted}${claim.denialReason ? '\nDenial: ' + claim.denialReason : ''}${claim.authorizationNumber ? '\nAuth #: ' + claim.authorizationNumber : ''}`,
                                  type: 'info',
                                })}
                              >
                                <FileText className="w-3 h-3 inline" />
                              </button>
                              {(claim.status === 'DENIED' || claim.status === 'PARTIAL') && (
                                <button
                                  className="ehr-button ehr-button-primary text-[9px] px-1.5 py-0.5"
                                  onClick={() => setAlert({
                                    title: 'Resubmit Claim',
                                    message: `Claim ${claim.claimNumber} for ${claim.patientName} has been queued for resubmission to ${claim.payer}.`,
                                    type: 'success',
                                  })}
                                >
                                  <RotateCcw className="w-3 h-3 inline mr-0.5" /> Resubmit
                                </button>
                              )}
                              {claim.status === 'DENIED' && (
                                <button
                                  className="ehr-button text-[9px] px-1.5 py-0.5"
                                  style={{ color: '#721c24' }}
                                  onClick={() => setAlert({
                                    title: 'Appeal Filed',
                                    message: `An appeal for claim ${claim.claimNumber} has been filed with ${claim.payer}.\nDenial reason: ${claim.denialReason}`,
                                    type: 'info',
                                  })}
                                >
                                  <AlertCircle className="w-3 h-3 inline mr-0.5" /> Appeal
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            );
          })}

          {filtered.length === 0 && (
            <div className="p-8 text-center text-gray-400 text-[11px]">
              No claims match the selected filter.
            </div>
          )}
        </div>
      </div>

      {/* Status Bar */}
      <div className="ehr-status-bar flex items-center justify-between">
        <span>
          Billing | {filtered.length} claim(s) | Total Billed: {fmt(totalBilled)} | Total Collected: {fmt(totalCollected)}
        </span>
        <span>Last refreshed: {new Date().toLocaleTimeString()}</span>
      </div>

      {/* Dialogs */}
      <PrintDialog
        isOpen={showPrintDialog}
        onClose={() => setShowPrintDialog(false)}
        onPrint={(options) => {
          setShowPrintDialog(false);
          setAlert({ title: 'Print Sent', message: `Claims report sent to printer (${options.action}).`, type: 'success' });
        }}
        title="Print Claims Report"
        documentName="Billing - Claims Report"
      />

      {alert && (
        <AlertDialog
          isOpen={true}
          onClose={() => setAlert(null)}
          title={alert.title}
          message={alert.message}
          type={alert.type}
        />
      )}
    </div>
  );
}

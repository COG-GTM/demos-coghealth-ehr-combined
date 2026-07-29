import { useCallback, useEffect, useState } from 'react';
import { Check, Clock3, Pill, RefreshCw, X } from 'lucide-react';
import { AlertDialog, ConfirmDialog } from '../components/ui/Modal';
import { refillRequestService } from '../services/refillRequestService';
import type { RefillRequest } from '../types';

type RefillDecision = 'approve' | 'deny';

function formatRequestedAt(requestedAt: string) {
  const date = new Date(requestedAt);
  return Number.isNaN(date.getTime()) ? requestedAt : date.toLocaleString('en-US', {
    month: '2-digit',
    day: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function RefillQueuePage() {
  const [requests, setRequests] = useState<RefillRequest[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedDecision, setSelectedDecision] = useState<{ request: RefillRequest; decision: RefillDecision } | null>(null);
  const [actionError, setActionError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const loadRequests = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const pendingRequests = await refillRequestService.getPending();
      setRequests(pendingRequests);
      window.dispatchEvent(new Event('refill-queue-updated'));
    } catch {
      setError('Unable to load pending refill requests. Confirm that the EHR API is running, then try again.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadRequests();
  }, [loadRequests]);

  const submitDecision = async () => {
    if (!selectedDecision) return;

    setIsSubmitting(true);
    setActionError('');
    try {
      const { request, decision } = selectedDecision;
      if (decision === 'approve') await refillRequestService.approve(request.id);
      else await refillRequestService.deny(request.id);
      setRequests(current => current.filter(item => item.id !== request.id));
      window.dispatchEvent(new Event('refill-queue-updated'));
      setSelectedDecision(null);
    } catch {
      setActionError(`Unable to ${selectedDecision.decision} this refill request. Please try again.`);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="h-full flex flex-col p-3 gap-3 overflow-auto">
      <div className="ehr-panel">
        <div className="ehr-header flex items-center justify-between">
          <div className="flex items-center">
            <Pill className="w-4 h-4 mr-2" />
            <span>Refill Queue</span>
            <span className="ml-2 px-1.5 py-0.5 bg-white/20 text-[10px]">{requests.length} pending</span>
          </div>
          <button className="ehr-toolbar-button flex items-center px-2 py-0.5" onClick={() => void loadRequests()} disabled={isLoading}>
            <RefreshCw className={`w-3 h-3 mr-1 ${isLoading ? 'animate-spin' : ''}`} /> Refresh
          </button>
        </div>
        <div className="ehr-subheader flex items-center justify-between">
          <span>Pending pharmacy refill requests requiring provider review</span>
          <span className="text-gray-500">Review queue</span>
        </div>

        <div className="bg-white overflow-auto">
          {isLoading ? (
            <div className="p-6 text-center text-[11px] text-gray-500">Loading pending refill requests...</div>
          ) : error ? (
            <div className="m-3 border border-red-300 bg-red-50 p-3 text-[11px] text-red-800">{error}</div>
          ) : requests.length === 0 ? (
            <div className="p-8 flex flex-col items-center text-center text-[11px] text-gray-500">
              <Check className="w-7 h-7 mb-2 text-green-600" />
              <span className="font-semibold text-gray-700">No pending refill requests</span>
              <span className="mt-1">All refill requests have been reviewed.</span>
            </div>
          ) : (
            <table className="w-full text-[11px]">
              <thead className="sticky top-0">
                <tr>
                  <th className="px-2 py-1 text-left">Patient</th>
                  <th className="px-2 py-1 text-left">Medication</th>
                  <th className="px-2 py-1 text-left">Pharmacy</th>
                  <th className="px-2 py-1 text-left">Requested</th>
                  <th className="px-2 py-1 text-center w-44">Actions</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((request, index) => (
                  <tr key={request.id} className={index % 2 === 1 ? 'bg-gray-50' : ''}>
                    <td className="px-2 py-2 border-b border-gray-200">
                      <div className="font-semibold">{request.patient.fullName}</div>
                      <div className="text-[10px] text-gray-500">{request.patient.mrn}</div>
                    </td>
                    <td className="px-2 py-2 border-b border-gray-200 font-medium">
                      {request.medication.genericName}
                      {request.medication.brandName && <div className="text-[10px] font-normal text-gray-500">{request.medication.brandName}</div>}
                    </td>
                    <td className="px-2 py-2 border-b border-gray-200">{request.pharmacyName}</td>
                    <td className="px-2 py-2 border-b border-gray-200 text-gray-600 whitespace-nowrap">
                      <div className="flex items-center"><Clock3 className="w-3 h-3 mr-1 text-gray-400" />{formatRequestedAt(request.requestedDate)}</div>
                    </td>
                    <td className="px-2 py-2 border-b border-gray-200 text-center whitespace-nowrap">
                      <button className="ehr-button ehr-button-primary px-2 py-0.5 mr-1" onClick={() => setSelectedDecision({ request, decision: 'approve' })}>
                        <Check className="w-3 h-3 inline mr-1" />Approve
                      </button>
                      <button className="ehr-button px-2 py-0.5" style={{ color: '#9b1c1c' }} onClick={() => setSelectedDecision({ request, decision: 'deny' })}>
                        <X className="w-3 h-3 inline mr-1" />Deny
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      <ConfirmDialog
        isOpen={selectedDecision !== null}
        onClose={() => !isSubmitting && setSelectedDecision(null)}
        onConfirm={() => void submitDecision()}
        title={selectedDecision?.decision === 'approve' ? 'Approve Refill Request' : 'Deny Refill Request'}
        message={selectedDecision ? `Are you sure you want to ${selectedDecision.decision} the refill request for ${selectedDecision.request.medication.genericName}?` : ''}
        confirmText={selectedDecision?.decision === 'approve' ? 'Approve Refill' : 'Deny Refill'}
        cancelText="Cancel"
        type={selectedDecision?.decision === 'deny' ? 'danger' : 'info'}
      />

      <AlertDialog
        isOpen={actionError !== ''}
        onClose={() => setActionError('')}
        title="Refill Request Not Updated"
        message={actionError}
        type="error"
      />
    </div>
  );
}

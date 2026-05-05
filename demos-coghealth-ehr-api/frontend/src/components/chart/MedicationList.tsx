import { useState } from 'react';
import { Plus, RefreshCw, Pause, X, History, Pill } from 'lucide-react';
import { Card, CardHeader, CardBody, Badge, Button, Modal, Input, EmptyState } from '../common';
import { formatDate, cn } from '../../utils';
import type { Medication } from '../../types';

interface MedicationListProps {
  medications: Medication[];
  onAdd?: (medication: Partial<Medication>) => void;
  onRefill?: (id: number) => void;
  onDiscontinue?: (id: number, reason: string) => void;
  isLoading?: boolean;
  compact?: boolean;
}

const statusColors = {
  ACTIVE: 'success',
  DISCONTINUED: 'danger',
  COMPLETED: 'gray',
  ON_HOLD: 'warning',
} as const;

export function MedicationList({ medications, onAdd, onRefill, onDiscontinue, isLoading, compact }: MedicationListProps) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [filter, setFilter] = useState<'active' | 'all' | 'discontinued'>('active');

  const activeMeds = medications.filter((m) => m.status === 'ACTIVE');
  const prnMeds = activeMeds.filter((m) => m.isPrn);
  const scheduledMeds = activeMeds.filter((m) => !m.isPrn);

  const filteredMeds = medications.filter((m) => {
    if (filter === 'active') return m.status === 'ACTIVE';
    if (filter === 'discontinued') return m.status === 'DISCONTINUED';
    return true;
  });

  if (compact) {
    return (
      <Card>
        <CardHeader className="flex items-center justify-between">
          <span>Medications</span>
          <Badge variant="info">{activeMeds.length}</Badge>
        </CardHeader>
        <CardBody className="p-0">
          {activeMeds.length === 0 ? (
            <div className="p-4 text-sm text-slate-500 text-center">No active medications</div>
          ) : (
            <ul className="divide-y">
              {activeMeds.slice(0, 5).map((med) => (
                <li key={med.id} className="px-4 py-2 text-sm">
                  <div className="font-medium text-slate-900">{med.drugName}</div>
                  <div className="text-slate-600">{med.sig}</div>
                  {med.isPrn && <Badge variant="warning" className="mt-1">PRN</Badge>}
                </li>
              ))}
              {activeMeds.length > 5 && (
                <li className="px-4 py-2 text-sm text-primary-600 font-medium">
                  +{activeMeds.length - 5} more
                </li>
              )}
            </ul>
          )}
        </CardBody>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setFilter('active')}
            className={cn('tab', filter === 'active' && 'active')}
          >
            Active ({activeMeds.length})
          </button>
          <button
            onClick={() => setFilter('discontinued')}
            className={cn('tab', filter === 'discontinued' && 'active')}
          >
            Discontinued
          </button>
          <button
            onClick={() => setFilter('all')}
            className={cn('tab', filter === 'all' && 'active')}
          >
            All
          </button>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm">
            <History className="w-4 h-4" />
            Reconcile
          </Button>
          {onAdd && (
            <Button size="sm" onClick={() => setShowAddModal(true)}>
              <Plus className="w-4 h-4" />
              New Rx
            </Button>
          )}
        </div>
      </div>

      {filteredMeds.length === 0 ? (
        <EmptyState
          icon={Pill}
          title="No medications found"
          description="No medications match the current filter"
          action={onAdd && <Button size="sm" onClick={() => setShowAddModal(true)}>Add Medication</Button>}
        />
      ) : (
        <div className="space-y-4">
          {filter === 'active' && scheduledMeds.length > 0 && (
            <Card>
              <CardHeader>Scheduled Medications</CardHeader>
              <div className="divide-y">
                {scheduledMeds.map((med) => (
                  <MedicationRow key={med.id} medication={med} onRefill={onRefill} onDiscontinue={onDiscontinue} />
                ))}
              </div>
            </Card>
          )}

          {filter === 'active' && prnMeds.length > 0 && (
            <Card>
              <CardHeader>PRN Medications</CardHeader>
              <div className="divide-y">
                {prnMeds.map((med) => (
                  <MedicationRow key={med.id} medication={med} onRefill={onRefill} onDiscontinue={onDiscontinue} />
                ))}
              </div>
            </Card>
          )}

          {filter !== 'active' && (
            <Card>
              <div className="divide-y">
                {filteredMeds.map((med) => (
                  <MedicationRow key={med.id} medication={med} onRefill={onRefill} onDiscontinue={onDiscontinue} />
                ))}
              </div>
            </Card>
          )}
        </div>
      )}

      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="New Prescription"
        size="lg"
        footer={
          <>
            <Button variant="secondary" onClick={() => setShowAddModal(false)}>Cancel</Button>
            <Button variant="secondary">Save to Orders</Button>
            <Button>E-Prescribe Now</Button>
          </>
        }
      >
        <div className="space-y-4">
          <Input label="Medication" placeholder="Search medications..." />
          <div className="grid grid-cols-3 gap-4">
            <Input label="Dose" placeholder="e.g., 500" />
            <div>
              <label className="label">Unit</label>
              <select className="input">
                <option>mg</option>
                <option>mcg</option>
                <option>g</option>
                <option>mL</option>
              </select>
            </div>
            <div>
              <label className="label">Route</label>
              <select className="input">
                <option>Oral</option>
                <option>Topical</option>
                <option>Injection</option>
                <option>Inhalation</option>
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Frequency</label>
              <select className="input">
                <option>Once daily</option>
                <option>Twice daily</option>
                <option>Three times daily</option>
                <option>Four times daily</option>
                <option>Every 4 hours</option>
                <option>Every 6 hours</option>
                <option>Every 8 hours</option>
                <option>At bedtime</option>
                <option>As needed</option>
              </select>
            </div>
            <Input label="Duration" placeholder="e.g., 30 days" />
          </div>
          <div>
            <label className="label">SIG (Directions)</label>
            <textarea className="input" rows={2} placeholder="Take 1 tablet by mouth once daily" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Input label="Quantity" type="number" placeholder="30" />
            <Input label="Refills" type="number" placeholder="3" />
          </div>
          <div>
            <label className="label">Indication</label>
            <Input placeholder="Search diagnosis..." />
          </div>
          <label className="flex items-center gap-2">
            <input type="checkbox" className="rounded" />
            <span className="text-sm">PRN (as needed)</span>
          </label>
        </div>
      </Modal>
    </div>
  );
}

function MedicationRow({
  medication,
  onRefill,
  onDiscontinue,
}: {
  medication: Medication;
  onRefill?: (id: number) => void;
  onDiscontinue?: (id: number, reason: string) => void;
}) {
  return (
    <div className="p-4 hover:bg-slate-50">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <span className="font-medium text-slate-900">{medication.drugName}</span>
            <Badge variant={statusColors[medication.status]}>{medication.status}</Badge>
            {medication.isPrn && <Badge variant="warning">PRN</Badge>}
          </div>
          <div className="text-sm text-slate-600 mt-1">{medication.sig}</div>
          <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
            <span>Started: {formatDate(medication.startDate)}</span>
            {medication.prescribedBy && (
              <span>Prescribed by: Dr. {medication.prescribedBy.lastName}</span>
            )}
            {medication.refills !== undefined && (
              <span>Refills: {medication.refills}</span>
            )}
          </div>
          {medication.indication && (
            <div className="text-xs text-slate-500 mt-1">For: {medication.indication}</div>
          )}
        </div>
        <div className="flex items-center gap-1">
          {medication.status === 'ACTIVE' && onRefill && (
            <button
              onClick={() => onRefill(medication.id)}
              className="p-2 text-slate-400 hover:text-primary-600 hover:bg-primary-50 rounded"
              title="Refill"
            >
              <RefreshCw className="w-4 h-4" />
            </button>
          )}
          {medication.status === 'ACTIVE' && onDiscontinue && (
            <button
              onClick={() => onDiscontinue(medication.id, '')}
              className="p-2 text-slate-400 hover:text-danger-600 hover:bg-danger-50 rounded"
              title="Discontinue"
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <button className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded" title="History">
            <History className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { Plus, Edit2, AlertTriangle, Check } from 'lucide-react';
import { Card, CardHeader, CardBody, Badge, Button, Modal, Input, EmptyState } from '../common';
import { formatDate, cn } from '../../utils';
import type { Allergy } from '../../types';

interface AllergyListProps {
  allergies: Allergy[];
  onAdd?: (allergy: Partial<Allergy>) => void;
  onUpdate?: (id: number, allergy: Partial<Allergy>) => void;
  isLoading?: boolean;
  compact?: boolean;
}

const severityColors = {
  MILD: 'success',
  MODERATE: 'warning',
  SEVERE: 'danger',
  LIFE_THREATENING: 'danger',
} as const;

const typeLabels = {
  MEDICATION: 'Medication',
  FOOD: 'Food',
  ENVIRONMENTAL: 'Environmental',
  OTHER: 'Other',
};

export function AllergyList({ allergies, onAdd, onUpdate, isLoading, compact }: AllergyListProps) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [showNKDA, setShowNKDA] = useState(false);

  const activeAllergies = allergies.filter((a) => a.status === 'ACTIVE');
  const medicationAllergies = activeAllergies.filter((a) => a.allergenType === 'MEDICATION');
  const otherAllergies = activeAllergies.filter((a) => a.allergenType !== 'MEDICATION');

  if (compact) {
    return (
      <Card>
        <CardHeader className="flex items-center justify-between">
          <span className="flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-warning-500" />
            Allergies
          </span>
          {activeAllergies.length > 0 ? (
            <Badge variant="danger">{activeAllergies.length}</Badge>
          ) : (
            <Badge variant="success">NKDA</Badge>
          )}
        </CardHeader>
        <CardBody className="p-0">
          {activeAllergies.length === 0 ? (
            <div className="p-4 text-sm text-slate-500 text-center">No known drug allergies</div>
          ) : (
            <ul className="divide-y">
              {activeAllergies.map((allergy) => (
                <li key={allergy.id} className="px-4 py-2 text-sm">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-slate-900">{allergy.allergen}</span>
                    <Badge variant={severityColors[allergy.severity]}>{allergy.severity}</Badge>
                  </div>
                  {allergy.reaction && (
                    <div className="text-slate-600 text-xs mt-0.5">{allergy.reaction}</div>
                  )}
                </li>
              ))}
            </ul>
          )}
        </CardBody>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold">Allergies & Adverse Reactions</h2>
        <div className="flex gap-2">
          {activeAllergies.length === 0 && (
            <Button variant="secondary" size="sm" onClick={() => setShowNKDA(true)}>
              <Check className="w-4 h-4" />
              Document NKDA
            </Button>
          )}
          {onAdd && (
            <Button size="sm" onClick={() => setShowAddModal(true)}>
              <Plus className="w-4 h-4" />
              Add Allergy
            </Button>
          )}
        </div>
      </div>

      {activeAllergies.length === 0 ? (
        <Card>
          <CardBody>
            <EmptyState
              icon={AlertTriangle}
              title="No Known Drug Allergies"
              description="No allergies have been documented for this patient"
              action={onAdd && <Button size="sm" onClick={() => setShowAddModal(true)}>Add Allergy</Button>}
            />
          </CardBody>
        </Card>
      ) : (
        <div className="space-y-4">
          {medicationAllergies.length > 0 && (
            <Card>
              <CardHeader>Medication Allergies</CardHeader>
              <div className="divide-y">
                {medicationAllergies.map((allergy) => (
                  <AllergyRow key={allergy.id} allergy={allergy} onUpdate={onUpdate} />
                ))}
              </div>
            </Card>
          )}

          {otherAllergies.length > 0 && (
            <Card>
              <CardHeader>Other Allergies</CardHeader>
              <div className="divide-y">
                {otherAllergies.map((allergy) => (
                  <AllergyRow key={allergy.id} allergy={allergy} onUpdate={onUpdate} />
                ))}
              </div>
            </Card>
          )}
        </div>
      )}

      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add Allergy"
        size="md"
        footer={
          <>
            <Button variant="secondary" onClick={() => setShowAddModal(false)}>Cancel</Button>
            <Button onClick={() => setShowAddModal(false)}>Save</Button>
          </>
        }
      >
        <div className="space-y-4">
          <div>
            <label className="label">Allergy Type</label>
            <select className="input">
              <option value="MEDICATION">Medication</option>
              <option value="FOOD">Food</option>
              <option value="ENVIRONMENTAL">Environmental</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <Input label="Allergen" placeholder="Search or enter allergen..." />
          <Input label="Reaction" placeholder="e.g., Hives, Anaphylaxis, Rash" />
          <div>
            <label className="label">Severity</label>
            <select className="input">
              <option value="MILD">Mild</option>
              <option value="MODERATE">Moderate</option>
              <option value="SEVERE">Severe</option>
              <option value="LIFE_THREATENING">Life Threatening</option>
            </select>
          </div>
          <Input label="Onset Date" type="date" />
          <div>
            <label className="label">Notes</label>
            <textarea className="input" rows={2} placeholder="Additional details..." />
          </div>
        </div>
      </Modal>
    </div>
  );
}

function AllergyRow({
  allergy,
  onUpdate,
}: {
  allergy: Allergy;
  onUpdate?: (id: number, allergy: Partial<Allergy>) => void;
}) {
  return (
    <div className="p-4 hover:bg-slate-50">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1">
          <div className="flex items-center gap-2">
            <span className="font-medium text-slate-900">{allergy.allergen}</span>
            <Badge variant={severityColors[allergy.severity]}>{allergy.severity}</Badge>
            <Badge variant="gray">{typeLabels[allergy.allergenType]}</Badge>
          </div>
          {allergy.reaction && (
            <div className="text-sm text-slate-600 mt-1">Reaction: {allergy.reaction}</div>
          )}
          <div className="flex items-center gap-4 mt-2 text-xs text-slate-500">
            {allergy.onsetDate && <span>Onset: {formatDate(allergy.onsetDate)}</span>}
            <span>Recorded: {formatDate(allergy.recordedAt)}</span>
            {allergy.verifiedAt && (
              <span className="text-success-600">Verified: {formatDate(allergy.verifiedAt)}</span>
            )}
          </div>
        </div>
        {onUpdate && (
          <button
            onClick={() => {}}
            className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded"
          >
            <Edit2 className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );
}

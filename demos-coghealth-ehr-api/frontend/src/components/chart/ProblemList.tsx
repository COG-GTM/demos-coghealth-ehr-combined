import { useState } from 'react';
import { Plus, Edit2, Check, X, AlertCircle } from 'lucide-react';
import { Card, CardHeader, CardBody, Badge, Button, Modal, Input, EmptyState } from '../common';
import { formatDate, cn } from '../../utils';
import type { Problem } from '../../types';

interface ProblemListProps {
  problems: Problem[];
  onAdd?: (problem: Partial<Problem>) => void;
  onUpdate?: (id: number, problem: Partial<Problem>) => void;
  isLoading?: boolean;
  compact?: boolean;
}

const statusColors = {
  ACTIVE: 'success',
  RESOLVED: 'gray',
  INACTIVE: 'gray',
} as const;

export function ProblemList({ problems, onAdd, onUpdate, isLoading, compact }: ProblemListProps) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [filter, setFilter] = useState<'all' | 'active' | 'resolved'>('active');

  const filteredProblems = problems.filter((p) => {
    if (filter === 'all') return true;
    if (filter === 'active') return p.status === 'ACTIVE';
    if (filter === 'resolved') return p.status === 'RESOLVED' || p.status === 'INACTIVE';
    return true;
  });

  const activeProblems = problems.filter((p) => p.status === 'ACTIVE');
  const resolvedProblems = problems.filter((p) => p.status !== 'ACTIVE');

  if (compact) {
    return (
      <Card>
        <CardHeader className="flex items-center justify-between">
          <span>Active Problems</span>
          <Badge variant="info">{activeProblems.length}</Badge>
        </CardHeader>
        <CardBody className="p-0">
          {activeProblems.length === 0 ? (
            <div className="p-4 text-sm text-slate-500 text-center">No active problems</div>
          ) : (
            <ul className="divide-y">
              {activeProblems.slice(0, 5).map((problem) => (
                <li key={problem.id} className="px-4 py-2 text-sm">
                  <div className="font-medium text-slate-900">{problem.icdCode}</div>
                  <div className="text-slate-600 truncate">{problem.description}</div>
                  {problem.onsetDate && (
                    <div className="text-xs text-slate-400 mt-0.5">
                      Onset: {formatDate(problem.onsetDate)}
                    </div>
                  )}
                </li>
              ))}
              {activeProblems.length > 5 && (
                <li className="px-4 py-2 text-sm text-primary-600 font-medium">
                  +{activeProblems.length - 5} more
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
            Active ({activeProblems.length})
          </button>
          <button
            onClick={() => setFilter('resolved')}
            className={cn('tab', filter === 'resolved' && 'active')}
          >
            Resolved ({resolvedProblems.length})
          </button>
          <button
            onClick={() => setFilter('all')}
            className={cn('tab', filter === 'all' && 'active')}
          >
            All ({problems.length})
          </button>
        </div>
        {onAdd && (
          <Button size="sm" onClick={() => setShowAddModal(true)}>
            <Plus className="w-4 h-4" />
            Add Problem
          </Button>
        )}
      </div>

      {filteredProblems.length === 0 ? (
        <EmptyState
          icon={AlertCircle}
          title="No problems found"
          description={filter === 'active' ? 'No active problems on file' : 'No problems match the current filter'}
          action={onAdd && <Button size="sm" onClick={() => setShowAddModal(true)}>Add Problem</Button>}
        />
      ) : (
        <Card>
          <table className="table">
            <thead>
              <tr>
                <th>ICD-10</th>
                <th>Description</th>
                <th>Status</th>
                <th>Onset</th>
                <th>Type</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filteredProblems.map((problem) => (
                <tr key={problem.id}>
                  <td className="font-mono text-sm">{problem.icdCode}</td>
                  <td>
                    <div className="font-medium">{problem.description}</div>
                    {problem.notes && (
                      <div className="text-xs text-slate-500 mt-0.5">{problem.notes}</div>
                    )}
                  </td>
                  <td>
                    <Badge variant={statusColors[problem.status]}>{problem.status}</Badge>
                  </td>
                  <td className="text-sm text-slate-600">
                    {problem.onsetDate ? formatDate(problem.onsetDate) : '--'}
                  </td>
                  <td>
                    {problem.isChronic && <Badge variant="info">Chronic</Badge>}
                    {problem.isPrincipal && <Badge variant="warning">Principal</Badge>}
                  </td>
                  <td>
                    {onUpdate && (
                      <button
                        onClick={() => {}}
                        className="p-1 text-slate-400 hover:text-slate-600"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add Problem"
        size="lg"
        footer={
          <>
            <Button variant="secondary" onClick={() => setShowAddModal(false)}>Cancel</Button>
            <Button onClick={() => setShowAddModal(false)}>Save</Button>
          </>
        }
      >
        <div className="space-y-4">
          <Input label="ICD-10 Code" placeholder="Search for diagnosis..." />
          <Input label="Description" />
          <div className="grid grid-cols-2 gap-4">
            <Input label="Onset Date" type="date" />
            <div>
              <label className="label">Status</label>
              <select className="input">
                <option value="ACTIVE">Active</option>
                <option value="RESOLVED">Resolved</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </div>
          </div>
          <div className="flex gap-4">
            <label className="flex items-center gap-2">
              <input type="checkbox" className="rounded" />
              <span className="text-sm">Chronic condition</span>
            </label>
            <label className="flex items-center gap-2">
              <input type="checkbox" className="rounded" />
              <span className="text-sm">Principal diagnosis</span>
            </label>
          </div>
          <div>
            <label className="label">Notes</label>
            <textarea className="input" rows={3} placeholder="Additional notes..." />
          </div>
        </div>
      </Modal>
    </div>
  );
}

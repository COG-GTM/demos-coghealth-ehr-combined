import { useState } from 'react';
import { ClipboardList, Play, RotateCcw, Save, Sparkles } from 'lucide-react';

interface PromptDashboardProps {
  onSubmitPrompt?: (prompt: string) => void;
}

export default function PromptDashboard({ onSubmitPrompt }: PromptDashboardProps) {
  const [prompt, setPrompt] = useState('');
  const [lastSubmittedPrompt, setLastSubmittedPrompt] = useState('');

  const handleSubmit = () => {
    const trimmedPrompt = prompt.trim();
    if (!trimmedPrompt) return;

    setLastSubmittedPrompt(trimmedPrompt);
    onSubmitPrompt?.(trimmedPrompt);
  };

  const handleReset = () => {
    setPrompt('');
    setLastSubmittedPrompt('');
  };

  return (
    <div className="ehr-panel h-full flex flex-col bg-[#ece9d8]">
      <div className="ehr-header flex items-center justify-between px-2">
        <div className="flex items-center">
          <ClipboardList className="w-3.5 h-3.5 mr-1.5" />
          <span>Prompt Dashboard</span>
        </div>
        <span className="text-[10px] text-blue-100">Clinical AI Workspace</span>
      </div>

      <div className="ehr-toolbar flex items-center justify-between">
        <div className="flex items-center space-x-1">
          <button className="ehr-toolbar-button flex items-center" onClick={handleSubmit} disabled={!prompt.trim()}>
            <Play className="w-3.5 h-3.5 mr-1" /> Run Prompt
          </button>
          <button className="ehr-toolbar-button flex items-center" onClick={handleReset}>
            <RotateCcw className="w-3.5 h-3.5 mr-1" /> Reset
          </button>
        </div>
        <button className="ehr-toolbar-button flex items-center" onClick={() => setLastSubmittedPrompt(prompt.trim())}>
          <Save className="w-3.5 h-3.5 mr-1" /> Save Draft
        </button>
      </div>

      <div className="flex-1 grid grid-cols-1 lg:grid-cols-[1fr_240px] gap-2 p-2 overflow-hidden">
        <div className="ehr-panel flex flex-col min-h-0">
          <div className="ehr-subheader flex items-center">
            <Sparkles className="w-3 h-3 mr-1" />
            Prompt Input
          </div>
          <div className="flex-1 p-2 flex flex-col min-h-0">
            <label htmlFor="prompt-dashboard-input" className="ehr-label mb-1">
              Enter prompt
            </label>
            <textarea
              id="prompt-dashboard-input"
              value={prompt}
              onChange={(event) => setPrompt(event.target.value)}
              placeholder="Example: Summarize the patient's recent labs and flag values outside the reference range."
              className="ehr-input flex-1 min-h-[180px] resize-none leading-4"
            />
            <div className="mt-1 flex items-center justify-between text-[10px] text-gray-600">
              <span>{prompt.length} characters</span>
              <span>{prompt.trim() ? 'Ready to run' : 'Waiting for prompt input'}</span>
            </div>
          </div>
        </div>

        <aside className="ehr-panel min-h-0 overflow-auto">
          <div className="ehr-subheader">Prompt Status</div>
          <div className="p-2 space-y-2">
            <div>
              <div className="ehr-label font-semibold">Last submitted</div>
              <div className="mt-1 bg-white border border-gray-300 p-2 min-h-16 text-[10px] text-gray-700">
                {lastSubmittedPrompt || 'No prompt submitted yet.'}
              </div>
            </div>
            <div className="ehr-alert-info p-2 text-[10px]">
              Prompts stay local until submitted by the parent workflow.
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}

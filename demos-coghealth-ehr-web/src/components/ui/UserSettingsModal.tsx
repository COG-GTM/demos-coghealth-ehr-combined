import { useState } from 'react';
import {
  User,
  Bell,
  Shield,
  Palette,
  Key,
  Monitor,
  Mail,
  Smartphone,
  Save,
  Check,
} from 'lucide-react';
import { Modal } from './Modal';

type SettingsTab = 'profile' | 'notifications' | 'security' | 'appearance';

interface UserProfile {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  npi: string;
  specialty: string;
  title: string;
}

const STORAGE_KEY = 'coghealth_settings';

const defaultProfile: UserProfile = {
  firstName: 'Sarah',
  lastName: 'Anderson',
  email: 'sarah.anderson@coghealth.com',
  phone: '(555) 123-4567',
  npi: '1234567890',
  specialty: 'Internal Medicine',
  title: 'MD',
};

const defaultNotifications = {
  emailAlerts: true,
  smsAlerts: false,
  labResults: true,
  appointments: true,
  messages: true,
  systemUpdates: false,
};

const defaultAppearance = {
  theme: 'light',
  compactMode: false,
  fontSize: 'medium',
};

interface UserSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function UserSettingsModal({ isOpen, onClose }: UserSettingsModalProps) {
  const [activeTab, setActiveTab] = useState<SettingsTab>('profile');
  const [saved, setSaved] = useState(false);

  const [profile, setProfile] = useState<UserProfile>(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const data = JSON.parse(stored);
        if (data.profile) return { ...defaultProfile, ...data.profile };
      }
    } catch (_) {}
    return defaultProfile;
  });

  const [notifications, setNotifications] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const data = JSON.parse(stored);
        if (data.notifications) return { ...defaultNotifications, ...data.notifications };
      }
    } catch (_) {}
    return defaultNotifications;
  });

  const [appearance, setAppearance] = useState(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const data = JSON.parse(stored);
        if (data.appearance) return { ...defaultAppearance, ...data.appearance };
      }
    } catch (_) {}
    return defaultAppearance;
  });

  const handleSave = () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ profile, notifications, appearance }));
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  const tabs: { id: SettingsTab; label: string; icon: React.ElementType }[] = [
    { id: 'profile', label: 'Profile', icon: User },
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'security', label: 'Security', icon: Shield },
    { id: 'appearance', label: 'Appearance', icon: Palette },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="User Settings"
      width="xl"
      footer={
        <div className="flex items-center justify-between w-full">
          <span className="text-[10px] text-gray-500">Dr. Sarah Anderson, MD &middot; Internal Medicine</span>
          <div className="flex items-center space-x-2">
            <button onClick={onClose} className="ehr-button px-4 text-[10px]">Cancel</button>
            <button
              onClick={handleSave}
              className={`ehr-button flex items-center text-[10px] px-4 ${saved ? '' : 'ehr-button-primary'}`}
              style={saved ? { background: 'linear-gradient(to bottom, #66cc66 0%, #339933 100%)', color: 'white', border: '1px solid #206020' } : undefined}
            >
              {saved ? <><Check className="w-3 h-3 mr-1" /> Saved</> : <><Save className="w-3 h-3 mr-1" /> Save Changes</>}
            </button>
          </div>
        </div>
      }
    >
      <div className="flex overflow-hidden" style={{ minHeight: '360px' }}>
        {/* Left tab navigation */}
        <div className="w-36 flex-shrink-0 border-r border-gray-400 pr-1 space-y-0.5 bg-[#ece9d8]">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`w-full flex items-center px-2 py-1.5 text-[11px] ${
                  activeTab === tab.id
                    ? 'bg-white border border-gray-400 font-semibold'
                    : 'hover:bg-white/50'
                }`}
              >
                <Icon className="w-3.5 h-3.5 mr-2 flex-shrink-0" />
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Right content */}
        <div className="flex-1 overflow-auto pl-3 bg-white">
          {activeTab === 'profile' && (
            <div className="space-y-3 pt-1">
              <fieldset className="ehr-fieldset">
                <legend>User Profile</legend>
                <div className="flex items-center space-x-3 mb-3">
                  <div className="w-12 h-12 bg-gray-100 flex items-center justify-center border border-gray-400 flex-shrink-0">
                    <span className="text-[11px] font-bold text-gray-700">
                      {profile.firstName[0]}{profile.lastName[0]}
                    </span>
                  </div>
                  <div>
                    <div className="text-[11px] font-semibold text-gray-800">Dr. {profile.firstName} {profile.lastName}, {profile.title}</div>
                    <div className="text-[10px] text-gray-500">{profile.specialty}</div>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">First Name</label>
                    <input
                      type="text"
                      value={profile.firstName}
                      onChange={(e) => setProfile({ ...profile, firstName: e.target.value })}
                      className="ehr-input w-full"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">Last Name</label>
                    <input
                      type="text"
                      value={profile.lastName}
                      onChange={(e) => setProfile({ ...profile, lastName: e.target.value })}
                      className="ehr-input w-full"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">Email</label>
                    <input
                      type="email"
                      value={profile.email}
                      onChange={(e) => setProfile({ ...profile, email: e.target.value })}
                      className="ehr-input w-full"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">Phone</label>
                    <input
                      type="tel"
                      value={profile.phone}
                      onChange={(e) => setProfile({ ...profile, phone: e.target.value })}
                      className="ehr-input w-full"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">NPI Number</label>
                    <input
                      type="text"
                      value={profile.npi}
                      onChange={(e) => setProfile({ ...profile, npi: e.target.value })}
                      className="ehr-input w-full font-mono"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">Specialty</label>
                    <select
                      value={profile.specialty}
                      onChange={(e) => setProfile({ ...profile, specialty: e.target.value })}
                      className="ehr-input w-full"
                    >
                      <option>Internal Medicine</option>
                      <option>Family Medicine</option>
                      <option>Pediatrics</option>
                      <option>Cardiology</option>
                      <option>Neurology</option>
                    </select>
                  </div>
                </div>
              </fieldset>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="space-y-3 pt-1">
              <fieldset className="ehr-fieldset">
                <legend>Notification Channels</legend>
                <div className="space-y-1.5">
                  <label className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300 cursor-pointer hover:bg-gray-100">
                    <div className="flex items-center">
                      <Mail className="w-3.5 h-3.5 text-gray-500 mr-2" />
                      <div>
                        <div className="text-[11px] font-medium">Email Notifications</div>
                        <div className="text-[10px] text-gray-500">Receive alerts via email</div>
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={notifications.emailAlerts}
                      onChange={(e) => setNotifications({ ...notifications, emailAlerts: e.target.checked })}
                      className="h-4 w-4"
                    />
                  </label>
                  <label className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300 cursor-pointer hover:bg-gray-100">
                    <div className="flex items-center">
                      <Smartphone className="w-3.5 h-3.5 text-gray-500 mr-2" />
                      <div>
                        <div className="text-[11px] font-medium">SMS Notifications</div>
                        <div className="text-[10px] text-gray-500">Receive urgent alerts via text</div>
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={notifications.smsAlerts}
                      onChange={(e) => setNotifications({ ...notifications, smsAlerts: e.target.checked })}
                      className="h-4 w-4"
                    />
                  </label>
                </div>
              </fieldset>
              <fieldset className="ehr-fieldset">
                <legend>Alert Types</legend>
                <div>
                  {[
                    { key: 'labResults', label: 'Lab Results', desc: 'When new lab results are available' },
                    { key: 'appointments', label: 'Appointments', desc: 'Reminders and schedule changes' },
                    { key: 'messages', label: 'Messages', desc: 'New messages from patients or staff' },
                    { key: 'systemUpdates', label: 'System Updates', desc: 'Maintenance and feature announcements' },
                  ].map((item, idx) => (
                    <label key={item.key} className={`flex items-center justify-between px-2 py-1.5 cursor-pointer hover:bg-blue-50 ${idx % 2 === 1 ? 'bg-gray-50' : ''}`}>
                      <div>
                        <div className="text-[11px] font-medium">{item.label}</div>
                        <div className="text-[10px] text-gray-500">{item.desc}</div>
                      </div>
                      <input
                        type="checkbox"
                        checked={notifications[item.key as keyof typeof notifications]}
                        onChange={(e) => setNotifications({ ...notifications, [item.key]: e.target.checked })}
                        className="h-4 w-4"
                      />
                    </label>
                  ))}
                </div>
              </fieldset>
            </div>
          )}

          {activeTab === 'security' && (
            <div className="space-y-3 pt-1">
              <fieldset className="ehr-fieldset">
                <legend>Security Settings</legend>
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300">
                    <div className="flex items-center">
                      <Key className="w-3.5 h-3.5 text-gray-500 mr-2" />
                      <div>
                        <div className="text-[11px] font-medium">Password</div>
                        <div className="text-[10px] text-gray-500">Last changed 30 days ago</div>
                      </div>
                    </div>
                    <button className="ehr-button text-[10px]">Change</button>
                  </div>
                  <div className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300">
                    <div className="flex items-center">
                      <Smartphone className="w-3.5 h-3.5 text-gray-500 mr-2" />
                      <div>
                        <div className="text-[11px] font-medium">Two-Factor Authentication</div>
                        <div className="text-[10px] text-gray-500">Add an extra layer of security</div>
                      </div>
                    </div>
                    <span className="px-1.5 py-0.5 bg-gray-100 border border-gray-400 text-gray-700 text-[9px]">Enabled</span>
                  </div>
                  <div className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300">
                    <div className="flex items-center">
                      <Monitor className="w-3.5 h-3.5 text-gray-500 mr-2" />
                      <div>
                        <div className="text-[11px] font-medium">Active Sessions</div>
                        <div className="text-[10px] text-gray-500">Manage your logged-in devices</div>
                      </div>
                    </div>
                    <button className="ehr-button text-[10px]">View</button>
                  </div>
                </div>
              </fieldset>
              <fieldset className="ehr-fieldset">
                <legend>Recent Activity</legend>
                <table className="w-full text-[10px]">
                  <tbody>
                    {[
                      { action: 'Login from Chrome on MacOS', time: '2 hours ago', location: 'San Francisco, CA' },
                      { action: 'Password changed', time: '30 days ago', location: 'San Francisco, CA' },
                      { action: 'Login from Safari on iPhone', time: '2 days ago', location: 'San Francisco, CA' },
                    ].map((activity, idx) => (
                      <tr key={idx} className={idx % 2 === 1 ? 'bg-gray-50' : ''}>
                        <td className="px-2 py-1">{activity.action}</td>
                        <td className="px-2 py-1 text-gray-500">{activity.location}</td>
                        <td className="px-2 py-1 text-gray-400 text-right">{activity.time}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </fieldset>
            </div>
          )}

          {activeTab === 'appearance' && (
            <div className="space-y-3 pt-1">
              <fieldset className="ehr-fieldset">
                <legend>Theme</legend>
                <div className="grid grid-cols-3 gap-2">
                  {['light', 'dark', 'system'].map((theme) => (
                    <button
                      key={theme}
                      onClick={() => setAppearance({ ...appearance, theme })}
                      className={`p-2 border text-center text-[11px] ${
                        appearance.theme === theme
                          ? 'border-gray-600 bg-white'
                          : 'border-gray-400 bg-gray-100 hover:bg-gray-50'
                      }`}
                    >
                      <Monitor className="w-4 h-4 mx-auto mb-1 text-gray-600" />
                      <span className="capitalize">{theme}</span>
                    </button>
                  ))}
                </div>
              </fieldset>
              <fieldset className="ehr-fieldset">
                <legend>Display Options</legend>
                <div className="space-y-2">
                  <div>
                    <label className="block text-[10px] text-gray-600 mb-0.5">Font Size</label>
                    <select
                      value={appearance.fontSize}
                      onChange={(e) => setAppearance({ ...appearance, fontSize: e.target.value })}
                      className="ehr-input w-48"
                    >
                      <option value="small">Small</option>
                      <option value="medium">Medium (Default)</option>
                      <option value="large">Large</option>
                    </select>
                  </div>
                  <label className="flex items-center justify-between p-2 bg-gray-50 border border-gray-300 cursor-pointer hover:bg-gray-100">
                    <div>
                      <div className="text-[11px] font-medium">Compact Mode</div>
                      <div className="text-[10px] text-gray-500">Reduce spacing for more content on screen</div>
                    </div>
                    <input
                      type="checkbox"
                      checked={appearance.compactMode}
                      onChange={(e) => setAppearance({ ...appearance, compactMode: e.target.checked })}
                      className="h-4 w-4"
                    />
                  </label>
                </div>
              </fieldset>
            </div>
          )}
        </div>
      </div>
    </Modal>
  );
}

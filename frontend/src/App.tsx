import { useState } from 'react';
import { Sparkles, LayoutDashboard } from 'lucide-react';
import { HomePage } from './pages/Home';
import { AnalyticsPage } from './pages/Analytics';
import './index.css';

type Page = 'home' | 'analytics';

export default function App() {
  const [page, setPage] = useState<Page>('home');

  return (
    <div className="app-layout">
      {/* Glossy Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <Sparkles size={24} />
          </div>
          <div>
            <div className="sidebar-logo-text">SmartCache AI</div>
            <div className="sidebar-logo-sub">Async Engine</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <button
            className={`nav-item ${page === 'home' ? 'active' : ''}`}
            onClick={() => setPage('home')}
          >
            <Sparkles size={18} />
            <span>Summarize</span>
          </button>
          <button
            className={`nav-item ${page === 'analytics' ? 'active' : ''}`}
            onClick={() => setPage('analytics')}
          >
            <LayoutDashboard size={18} />
            <span>Analytics</span>
          </button>
        </nav>
      </aside>

      {/* Main Container */}
      <main className="main-content">
        <div className="page-container">
          {page === 'home' && <HomePage />}
          {page === 'analytics' && <AnalyticsPage />}
        </div>
      </main>
    </div>
  );
}

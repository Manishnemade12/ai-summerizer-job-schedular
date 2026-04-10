import { useCallback, useEffect, useState } from 'react';
import { Activity, Database, Server, Zap, RefreshCw, XCircle, Clock } from 'lucide-react';
import { getAnalytics, type Analytics } from '../services/api';

export function AnalyticsPage() {
  const [metrics, setMetrics] = useState<Analytics | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchMetrics = useCallback(async () => {
    try {
      const data = await getAnalytics();
      setMetrics(data);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMetrics();
    const interval = setInterval(fetchMetrics, 10000);
    return () => clearInterval(interval);
  }, [fetchMetrics]);

  const hitRate =
    metrics && metrics.total_requests > 0
      ? Math.round((metrics.cache_hits / metrics.total_requests) * 100)
      : 0;

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 40 }}>
        <div>
          <h1 className="page-title">System Analytics</h1>
          <p className="page-subtitle">Live metrics tracking performance, processing latency, and Valkey cache efficiency. Auto-refreshes every 10s.</p>
        </div>
        <button 
          onClick={fetchMetrics}
          style={{ 
            background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-light)', 
            color: 'white', padding: '8px 16px', borderRadius: '30px', 
            cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 
          }}
        >
          <RefreshCw size={14} className={loading ? 'spinner-ring border-none' : ''} style={loading ? { animation: 'spin 1s linear infinite' } : {}} />
          Refresh
        </button>
      </div>

      {error && <div className="error-box" style={{ marginBottom: 24 }}>⚠️ {error}</div>}

      {loading && !metrics && (
        <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: 60 }}>
          <div className="spinner-ring" style={{ width: 32, height: 32, marginBottom: 16 }} />
          <p style={{ color: 'var(--text-secondary)' }}>Gathering real-time metrics...</p>
        </div>
      )}

      {metrics && (
        <>
          <div className="neon-grid">
            <div className="neon-card cyan">
              <div className="neon-icon"><Activity size={20} className="text-cyan" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{metrics.total_requests.toLocaleString()}</div>
                <div className="neon-label">Total Requests</div>
              </div>
            </div>

            <div className="neon-card emerald">
              <div className="neon-icon"><Zap size={20} className="text-emerald" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{metrics.cache_hits.toLocaleString()}</div>
                <div className="neon-label">Cache Hits</div>
              </div>
            </div>

            <div className="neon-card amber">
              <div className="neon-icon"><Database size={20} className="text-amber" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{metrics.cache_misses.toLocaleString()}</div>
                <div className="neon-label">Cache Misses</div>
              </div>
            </div>

            <div className="neon-card violet">
              <div className="neon-icon"><Server size={20} className="text-violet" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{metrics.queue_size}</div>
                <div className="neon-label">Queue Size</div>
              </div>
            </div>

            <div className="neon-card cyan">
              <div className="neon-icon"><Clock size={20} className="text-cyan" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{Math.round(metrics.avg_processing_time_ms)}<span style={{fontSize: 16, marginLeft: 4, color: 'var(--text-muted)'}}>ms</span></div>
                <div className="neon-label">Avg Processing Time</div>
              </div>
            </div>

            <div className="neon-card red">
              <div className="neon-icon"><XCircle size={20} className="text-red" /></div>
              <div style={{ flex: 1 }}>
                <div className="neon-value">{metrics.failed_jobs}</div>
                <div className="neon-label">Failed Jobs</div>
              </div>
            </div>
          </div>

          <div className="glass-card">
            <h3 className="card-title"><Zap className="text-emerald" size={20} /> Cache Hit Efficiency</h3>
            <div style={{ marginTop: 24 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12, fontSize: 16 }}>
                <span style={{ color: 'var(--text-secondary)' }}>Hit Rate</span>
                <strong style={{ color: hitRate > 60 ? 'var(--success)' : 'var(--warning)', fontSize: 20 }}>
                  {hitRate}%
                </strong>
              </div>
              <div style={{ height: 12, background: 'rgba(0,0,0,0.3)', borderRadius: 6, overflow: 'hidden' }}>
                <div 
                  style={{ 
                    height: '100%', 
                    width: `${hitRate}%`, 
                    background: 'linear-gradient(90deg, var(--accent-cyan), var(--success))',
                    borderRadius: 6,
                    transition: 'width 1s cubic-bezier(0.4, 0, 0.2, 1)' 
                  }} 
                />
              </div>
              <p style={{ marginTop: 16, color: 'var(--text-muted)', fontSize: 14 }}>
                {hitRate > 60
                  ? '✅ High efficiency. Valkey is significantly reducing LLM API calls and costs.'
                  : hitRate > 30
                  ? '⚠️ Cache is warming up. More requests will increase efficiency.'
                  : 'ℹ️ Low cache utilization. Currently processing mostly unique URLs or texts.'}
              </p>
            </div>
          </div>
        </>
      )}
    </>
  );
}

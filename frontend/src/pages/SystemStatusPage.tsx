import React, { useState, useEffect } from 'react';
import { SystemHealthReport } from '../types';
import { systemApi } from '../api/client';
import {
  Activity,
  Database,
  Cpu,
  Shield,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Clock,
} from 'lucide-react';

export const SystemStatusPage: React.FC = () => {
  const [health, setHealth] = useState<SystemHealthReport | null>(null);
  const [detail, setDetail] = useState<'summary' | 'engine' | 'session'>('engine');
  const [trackSession, setTrackSession] = useState<boolean>(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadHealth = async (mode = detail, track = trackSession) => {
    setLoading(true);
    setError(null);
    try {
      const report = await systemApi.getHealth(mode, track);
      setHealth(report);
    } catch (err: any) {
      setError(err.message || 'Unable to reach /servlet/system-health');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHealth(detail, trackSession);
  }, [detail, trackSession]);

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-xs text-brand-teal font-semibold uppercase tracking-wider mb-1">
              <Activity className="w-4 h-4" />
              <span>Jakarta HttpServlet Diagnostics Endpoint</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">
              Live System Health & Session Monitor
            </h1>
            <p className="text-xs text-slate-400 mt-1 max-w-2xl">
              Served directly by <code className="text-brand-tealLight">/servlet/system-health</code> (<code className="text-slate-300">SystemHealthServlet extends HttpServlet</code>) running alongside Spring MVC on embedded Tomcat. Secrets, JDBC URLs, and raw session IDs are masked.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2 text-xs">
            <select
              aria-label="Servlet detail query parameter"
              value={detail}
              onChange={(e) => setDetail(e.target.value as 'summary' | 'engine' | 'session')}
              className="bg-navy-950 border border-navy-700 rounded-xl px-3 py-2 text-white font-semibold"
            >
              <option value="engine">detail=engine</option>
              <option value="summary">detail=summary</option>
              <option value="session">detail=session</option>
            </select>

            <label className="flex items-center space-x-2 bg-navy-950 border border-navy-700 rounded-xl px-3 py-2 cursor-pointer">
              <input
                type="checkbox"
                checked={trackSession}
                onChange={(e) => setTrackSession(e.target.checked)}
                className="accent-brand-teal"
              />
              <span className="text-slate-200 font-semibold">trackSession</span>
            </label>

            <button
              type="button"
              onClick={() => loadHealth(detail, trackSession)}
              disabled={loading}
              className="px-4 py-2 rounded-xl bg-brand-violet hover:bg-brand-violetLight text-white font-bold flex items-center space-x-1.5 shadow-md shadow-brand-violet/25 disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              <span>Probe Servlet</span>
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-300 text-xs flex items-center space-x-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {health && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            {/* Database Status */}
            <div className="bg-navy-900 border border-navy-800 rounded-2xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Database className="w-5 h-5 text-blue-400" />
                  <h2 className="text-sm font-bold text-white">JDBC DataSource</h2>
                </div>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold border flex items-center space-x-1 ${
                    health.database.connected
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                      : 'bg-rose-500/10 text-rose-400 border-rose-500/30'
                  }`}
                >
                  {health.database.connected ? <CheckCircle2 className="w-3 h-3" /> : <AlertTriangle className="w-3 h-3" />}
                  <span>{health.database.connected ? 'CONNECTED' : 'UNAVAILABLE'}</span>
                </span>
              </div>

              <div className="space-y-1.5 text-xs bg-navy-950/70 border border-navy-800 rounded-xl p-3">
                <div className="flex justify-between">
                  <span className="text-slate-400">Engine Product</span>
                  <span className="text-white font-semibold">{health.database.productName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Validation Query Latency</span>
                  <span className="text-brand-teal font-mono font-bold">{health.database.latencyMs} ms</span>
                </div>
                <div className="text-[11px] text-slate-400 pt-1 border-t border-navy-800">
                  {health.database.message}
                </div>
              </div>
            </div>

            {/* Recommendation Engine Status */}
            <div className="bg-navy-900 border border-navy-800 rounded-2xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Cpu className="w-5 h-5 text-brand-teal" />
                  <h2 className="text-sm font-bold text-white">Recommendation Engine</h2>
                </div>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${
                    health.recommendationEngine.available
                      ? 'bg-brand-teal/15 text-brand-tealLight border-brand-teal/30'
                      : 'bg-rose-500/10 text-rose-400 border-rose-500/30'
                  }`}
                >
                  {health.recommendationEngine.status}
                </span>
              </div>

              <div className="space-y-1.5 text-xs bg-navy-950/70 border border-navy-800 rounded-xl p-3">
                <div className="flex justify-between">
                  <span className="text-slate-400">Catalog Opportunities</span>
                  <span className="text-white font-semibold">
                    {health.recommendationEngine.catalogOpportunities ?? 'Hidden in session mode'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Thread Pool (Active / Core)</span>
                  <span className="text-white font-mono font-semibold">
                    {health.recommendationEngine.activeThreads ?? '-'} / {health.recommendationEngine.corePoolSize ?? '-'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Telemetry Counters Tracked</span>
                  <span className="text-brand-violetLight font-mono font-semibold">
                    {health.recommendationEngine.trackedTelemetryItems ?? '-'}
                  </span>
                </div>
              </div>
            </div>

            {/* HttpSession Status */}
            <div className="bg-navy-900 border border-navy-800 rounded-2xl p-5 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Shield className="w-5 h-5 text-brand-violetLight" />
                  <h2 className="text-sm font-bold text-white">HttpSession Tracking</h2>
                </div>
                <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold border bg-brand-violet/15 text-brand-violetLight border-brand-violet/30">
                  {health.session.active ? 'SESSION ACTIVE' : 'PASSIVE'}
                </span>
              </div>

              <div className="space-y-1.5 text-xs bg-navy-950/70 border border-navy-800 rounded-xl p-3">
                <div className="flex justify-between">
                  <span className="text-slate-400">Authenticated User</span>
                  <span className="text-white font-semibold">
                    {health.session.authenticated
                      ? health.session.activeStudentName || 'Signed In'
                      : 'Guest'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Session Health Checks</span>
                  <span className="text-brand-teal font-mono font-bold">{health.session.statusCheckCount}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Tracking Mode</span>
                  <span className="text-slate-300 font-medium">{health.session.trackingMode}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Servlet Lifecycle Metadata Bar */}
          <div className="bg-navy-900 border border-navy-800 rounded-2xl p-4 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-300">
            <div className="flex items-center space-x-2">
              <Clock className="w-4 h-4 text-brand-teal" />
              <span>
                Service: <strong className="text-white">{health.service}</strong> ({health.status})
              </span>
            </div>
            <div>
              Servlet Uptime: <strong className="text-white">{health.uptimeSeconds}s</strong>
            </div>
            <div>
              Total Requests Served Since <code className="text-brand-violetLight">init()</code>:{' '}
              <strong className="text-white">{health.servletRequestCount}</strong>
            </div>
            <div className="text-slate-400">
              Mode: <code className="text-slate-200">{health.detailMode}</code>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

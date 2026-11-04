import React, { useState, useEffect } from 'react';
import { EvaluationResult } from '../types';
import { evaluationApi } from '../api/client';
import { BarChart3, TrendingUp, Sparkles, RefreshCw, CheckCircle, Info, Layers, Award } from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
} from 'recharts';

export const EvaluationPage: React.FC = () => {
  const [result, setResult] = useState<EvaluationResult | null>(null);
  const [topK, setTopK] = useState(5);
  const [loading, setLoading] = useState(false);

  const runEvaluation = async () => {
    setLoading(true);
    try {
      const data = await evaluationApi.run(topK);
      setResult(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runEvaluation();
  }, [topK]);

  const metricsChartData = result
    ? [
        {
          metric: 'Precision@K',
          'PathPilot Hybrid AI': Number((result.hybridPrecisionAtK * 100).toFixed(1)),
          'Popularity Baseline': Number((result.baselinePrecisionAtK * 100).toFixed(1)),
        },
        {
          metric: 'Recall@K',
          'PathPilot Hybrid AI': Number((result.hybridRecallAtK * 100).toFixed(1)),
          'Popularity Baseline': Number((result.baselineRecallAtK * 100).toFixed(1)),
        },
        {
          metric: 'NDCG@K',
          'PathPilot Hybrid AI': Number((result.hybridNdcgAtK * 100).toFixed(1)),
          'Popularity Baseline': Number((result.baselineNdcgAtK * 100).toFixed(1)),
        },
        {
          metric: 'Catalog Coverage',
          'PathPilot Hybrid AI': Number(result.hybridCatalogCoverage.toFixed(1)),
          'Popularity Baseline': Number(result.baselineCatalogCoverage.toFixed(1)),
        },
      ]
    : [];

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="bg-gradient-to-r from-navy-900 via-navy-850 to-navy-900 border border-navy-800 rounded-2xl p-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-xs text-brand-teal font-semibold uppercase tracking-wider mb-1">
              <BarChart3 className="w-4 h-4" />
              <span>Offline IR Evaluation Module</span>
            </div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight">
              Recommendation Quality & Baseline Benchmark
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Rigorous comparative evaluation measuring Precision@K, Recall@K, NDCG@K, and Catalog Diversity against a standard Popularity Baseline.
            </p>
          </div>

          <div className="flex items-center space-x-3">
            <span className="text-xs text-slate-300 font-medium">Cutoff (K):</span>
            <select
              aria-label="Evaluation Cutoff (K)"
              value={topK}
              onChange={(e) => setTopK(Number(e.target.value))}
              className="bg-navy-950 border border-navy-700 text-xs font-bold text-white rounded-xl px-3 py-2"
            >
              <option value={3}>Top-3</option>
              <option value={5}>Top-5</option>
              <option value={8}>Top-8</option>
              <option value={10}>Top-10</option>
            </select>

            <button
              onClick={runEvaluation}
              disabled={loading}
              className="px-4 py-2 bg-brand-violet hover:bg-brand-violetLight text-white rounded-xl text-xs font-bold flex items-center space-x-1.5 transition-all shadow-md shadow-brand-violet/25 disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              <span>Re-run Evaluation</span>
            </button>
          </div>
        </div>
      </div>

      {/* Metrics Summary Cards */}
      {result && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Precision@K Card */}
          <div className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card">
            <div className="flex items-center justify-between text-xs text-slate-400 mb-2 font-medium">
              <span>Precision@{topK}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-brand-teal/10 text-brand-teal font-bold">
                Relevance Ratio
              </span>
            </div>
            <div className="text-3xl font-extrabold text-white tracking-tight">
              {(result.hybridPrecisionAtK * 100).toFixed(1)}%
            </div>
            <div className="text-xs text-slate-400 mt-2 flex items-center space-x-1">
              <span>Baseline:</span>
              <span className="text-slate-300 font-bold">{(result.baselinePrecisionAtK * 100).toFixed(1)}%</span>
              <span className="text-brand-teal font-semibold">
                (+{((result.hybridPrecisionAtK - result.baselinePrecisionAtK) * 100).toFixed(1)}%)
              </span>
            </div>
          </div>

          {/* Recall@K Card */}
          <div className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card">
            <div className="flex items-center justify-between text-xs text-slate-400 mb-2 font-medium">
              <span>Recall@{topK}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-brand-violet/15 text-brand-violetLight font-bold">
                Catalog Capture
              </span>
            </div>
            <div className="text-3xl font-extrabold text-white tracking-tight">
              {(result.hybridRecallAtK * 100).toFixed(1)}%
            </div>
            <div className="text-xs text-slate-400 mt-2 flex items-center space-x-1">
              <span>Baseline:</span>
              <span className="text-slate-300 font-bold">{(result.baselineRecallAtK * 100).toFixed(1)}%</span>
              <span className="text-brand-teal font-semibold">
                (+{((result.hybridRecallAtK - result.baselineRecallAtK) * 100).toFixed(1)}%)
              </span>
            </div>
          </div>

          {/* NDCG@K Card */}
          <div className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card">
            <div className="flex items-center justify-between text-xs text-slate-400 mb-2 font-medium">
              <span>NDCG@{topK}</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-amber-500/10 text-amber-400 font-bold">
                Ranking Quality
              </span>
            </div>
            <div className="text-3xl font-extrabold text-white tracking-tight">
              {(result.hybridNdcgAtK * 100).toFixed(1)}%
            </div>
            <div className="text-xs text-slate-400 mt-2 flex items-center space-x-1">
              <span>Baseline:</span>
              <span className="text-slate-300 font-bold">{(result.baselineNdcgAtK * 100).toFixed(1)}%</span>
              <span className="text-brand-teal font-semibold">
                (+{((result.hybridNdcgAtK - result.baselineNdcgAtK) * 100).toFixed(1)}%)
              </span>
            </div>
          </div>

          {/* Catalog Coverage Card */}
          <div className="bg-navy-900 border border-navy-800 rounded-xl p-5 glow-card">
            <div className="flex items-center justify-between text-xs text-slate-400 mb-2 font-medium">
              <span>Catalog Coverage</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-purple-500/10 text-purple-400 font-bold">
                Catalog Long Tail
              </span>
            </div>
            <div className="text-3xl font-extrabold text-white tracking-tight">
              {result.hybridCatalogCoverage.toFixed(1)}%
            </div>
            <div className="text-xs text-slate-400 mt-2 flex items-center space-x-1">
              <span>Baseline:</span>
              <span className="text-slate-300 font-bold">{result.baselineCatalogCoverage.toFixed(1)}%</span>
              <span className="text-brand-teal font-semibold">
                (+{(result.hybridCatalogCoverage - result.baselineCatalogCoverage).toFixed(1)}%)
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Comparison Chart */}
      <div className="bg-navy-900 border border-navy-800 rounded-2xl p-6">
        <h3 className="text-base font-bold text-white mb-1">
          Comparative Performance: PathPilot Hybrid AI vs Popularity Baseline
        </h3>
        <p className="text-xs text-slate-400 mb-6">
          Evaluated across all test student profiles with varied constraints, background proficiencies, and target career paths.
        </p>

        <div className="h-80 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={metricsChartData} margin={{ top: 10, right: 20, left: -10, bottom: 10 }}>
              <XAxis dataKey="metric" stroke="#64748b" fontSize={12} tickLine={false} />
              <YAxis stroke="#64748b" fontSize={12} domain={[0, 100]} unit="%" />
              <Tooltip
                contentStyle={{ backgroundColor: '#0c1424', borderColor: '#223456', borderRadius: '8px', fontSize: '12px' }}
                labelStyle={{ color: '#ffffff', fontWeight: 'bold' }}
              />
              <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '15px' }} />
              <Bar dataKey="PathPilot Hybrid AI" fill="#14b8a6" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Popularity Baseline" fill="#475569" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Mathematical Methodology Explanation */}
      <div className="bg-navy-900 border border-navy-800 rounded-2xl p-6 space-y-4 text-xs text-slate-300 leading-relaxed">
        <h3 className="text-base font-bold text-white flex items-center space-x-2">
          <Info className="w-4 h-4 text-brand-teal" />
          <span>Evaluation Methodology & Mathematical Definitions</span>
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-navy-950/70 p-4 rounded-xl border border-navy-800">
            <h4 className="font-bold text-white mb-1">1. Precision@K</h4>
            <p className="text-slate-400 mb-2">
              Measures the proportion of recommended items in top-K that satisfy core target role competencies:
            </p>
            <div className="font-mono bg-navy-900 p-2 rounded text-brand-teal text-[11px]">
              Precision@K = |Relevant ∩ TopK| / K
            </div>
          </div>

          <div className="bg-navy-950/70 p-4 rounded-xl border border-navy-800">
            <h4 className="font-bold text-white mb-1">2. Recall@K</h4>
            <p className="text-slate-400 mb-2">
              Measures the percentage of all eligible relevant items in the catalog retrieved in the top-K:
            </p>
            <div className="font-mono bg-navy-900 p-2 rounded text-brand-teal text-[11px]">
              Recall@K = |Relevant ∩ TopK| / |All Relevant in Catalog|
            </div>
          </div>

          <div className="bg-navy-950/70 p-4 rounded-xl border border-navy-800">
            <h4 className="font-bold text-white mb-1">3. NDCG@K (Ranking Quality)</h4>
            <p className="text-slate-400 mb-2">
              Discounts items ranked lower using logarithmic position penalties:
            </p>
            <div className="font-mono bg-navy-900 p-2 rounded text-brand-teal text-[11px]">
              DCG@K = Σ (rel_i / log2(i + 1)), NDCG@K = DCG@K / IDCG@K
            </div>
          </div>

          <div className="bg-navy-950/70 p-4 rounded-xl border border-navy-800">
            <h4 className="font-bold text-white mb-1">4. Catalog Coverage</h4>
            <p className="text-slate-400 mb-2">
              Demonstrates lack of popularity bias by evaluating the spread of unique catalog items recommended across users:
            </p>
            <div className="font-mono bg-navy-900 p-2 rounded text-brand-teal text-[11px]">
              Coverage = |Unique Recommended Opportunities| / |Catalog Size|
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

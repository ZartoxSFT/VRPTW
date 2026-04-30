import re
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parents[1]
CONSOLIDATED = ROOT / "campaign3_consolidated_20260428_080236.csv"


def load_data() -> pd.DataFrame:
    if CONSOLIDATED.exists():
        df = pd.read_csv(CONSOLIDATED)
        source = CONSOLIDATED.name
    else:
        logs = list((ROOT / "resultsSA").rglob("executions_log.csv"))
        frames = []
        for log in logs:
            try:
                frames.append(pd.read_csv(log))
            except Exception:
                pass
        if not frames:
            raise SystemExit("No SA results found. Run the sweep first.")
        df = pd.concat(frames, ignore_index=True)
        source = f"{len(logs)} execution logs"
    print(f"Source: {source}")
    return df


def parse_params(value: str):
    if not isinstance(value, str):
        return None, None, None, None
    penalty = re.search(r"penaltyWeight=(\d+(?:\.\d+)?)", value)
    temp = re.search(r"initialTemp=(\d+(?:\.\d+)?)", value)
    cooling = re.search(r"coolingRate=(\d+(?:\.\d+)?)", value)
    iterations = re.search(r"iterations=(\d+)", value)
    return (
        float(penalty.group(1)) if penalty else None,
        float(temp.group(1)) if temp else None,
        float(cooling.group(1)) if cooling else None,
        int(iterations.group(1)) if iterations else None,
    )


def main():
    df = load_data()

    df = df[
        (df["instance"] == "data101.vrp")
        & (df["algorithm"].astype(str).str.upper() == "SA")
        & (df["enforce_time_windows"].astype(str).str.lower().isin(["true", "1"]))
    ].copy()

    if df.empty:
        raise SystemExit("No SA + TW runs found.")

    parsed = df["parameters"].apply(parse_params)
    df[["penalty", "temp", "cooling", "iterations"]] = pd.DataFrame(parsed.tolist(), index=df.index)
    df = df[df[["penalty", "temp", "cooling", "iterations"]].notna().all(axis=1)].copy()

    if df.empty:
        raise SystemExit("No complete sweep runs found after parameter parsing.")

    feasible = df[
        (df["time_violation"] == 0.0)
        & (df["capacity_violation"] == 0.0)
        & (df["vehicle_violation"] == 0.0)
    ].copy()

    work = feasible if not feasible.empty else df
    label = "feasible runs (no violations)" if not feasible.empty else "all parsed TW runs"

    print("=" * 120)
    print("SA NATURAL SWEEP ANALYSIS - data101.vrp WITH TW ONLY")
    print("=" * 120)
    print(f"\nRows analyzed: {len(work)} ({label})")
    print(f"Unique penalties: {work['penalty'].nunique()}")
    print(f"Unique temps: {work['temp'].nunique()}")
    print(f"Unique coolings: {work['cooling'].nunique()}")
    print(f"Unique iterations: {work['iterations'].nunique()}")
    print(f"Routes min/max: {work['routes'].min():.0f} / {work['routes'].max():.0f}")

    best = work.loc[work["best_distance"].idxmin()]
    prof_ref = 1650.80
    gap = best["best_distance"] - prof_ref

    print("\n" + "=" * 120)
    print("BEST NATURAL FEASIBLE SOLUTION")
    print("=" * 120)
    print(f"\nDistance:    {best['best_distance']:.2f} km")
    print(f"Routes:      {best['routes']:.0f}")
    print(f"Penalty:     {best['penalty']:.0f}")
    print(f"Temp:        {best['temp']:.0f}")
    print(f"Cooling:     {best['cooling']:.4f}")
    print(f"Iterations:  {best['iterations']:.0f}")
    print(f"Runtime:     {best['runtime_ms']:.0f} ms")
    print(f"Vs prof:     {gap:+.2f} km ({100 * gap / prof_ref:+.1f}%)")

    print("\n" + "=" * 120)
    print("TOP 10 FEASIBLE SOLUTIONS")
    print("=" * 120)
    top10 = work.nsmallest(10, "best_distance")
    for i, (_, row) in enumerate(top10.iterrows(), 1):
        print(
            f"{i:2d}. {row['best_distance']:7.2f} km | routes={row['routes']:2.0f} | "
            f"penalty={row['penalty']:>7.0f} | temp={row['temp']:>5.0f} | "
            f"cool={row['cooling']:.4f} | iter={row['iterations']:.0f}"
        )

    print("\n" + "=" * 120)
    print("TOP 10 CLOSEST TO 1650.80 km (comparison only)")
    print("=" * 120)
    work = work.copy()
    work["gap_to_prof"] = (work["best_distance"] - prof_ref).abs()
    closest10 = work.nsmallest(10, "gap_to_prof")
    for i, (_, row) in enumerate(closest10.iterrows(), 1):
        print(
            f"{i:2d}. {row['best_distance']:7.2f} km | gap={row['gap_to_prof']:6.2f} | "
            f"routes={row['routes']:2.0f} | penalty={row['penalty']:>7.0f} | "
            f"temp={row['temp']:>5.0f} | cool={row['cooling']:.4f} | iter={row['iterations']:.0f}"
        )

    print("\n" + "=" * 120)
    print("SENSITIVITY BY ITERATIONS")
    print("=" * 120)
    by_iter = work.groupby("iterations")["best_distance"].agg(["min", "mean", "count"]).round(2)
    print(by_iter.to_string())

    if {50000, 100000}.issubset(set(work["iterations"].astype(int).unique())):
        min_50k = work[work["iterations"] == 50000]["best_distance"].min()
        min_100k = work[work["iterations"] == 100000]["best_distance"].min()
        print(f"\n50k best:  {min_50k:.2f} km")
        print(f"100k best: {min_100k:.2f} km")
        print(f"Delta:     {min_50k - min_100k:+.2f} km")

    print("\n" + "=" * 120)
    print("SENSITIVITY BY PENALTY")
    print("=" * 120)
    by_penalty = work.groupby("penalty")["best_distance"].agg(["min", "mean", "count"]).round(2)
    print(by_penalty.to_string())

    print("\n" + "=" * 120)
    print("SENSITIVITY BY TEMPERATURE")
    print("=" * 120)
    by_temp = work.groupby("temp")["best_distance"].agg(["min", "mean", "count"]).round(2)
    print(by_temp.to_string())

    print("\n" + "=" * 120)
    print("SENSITIVITY BY COOLING")
    print("=" * 120)
    by_cooling = work.groupby("cooling")["best_distance"].agg(["min", "mean", "count"]).round(2)
    print(by_cooling.to_string())

    print("\n" + "=" * 120)
    print("FINAL TAKEAWAY")
    print("=" * 120)
    print(f"\nBest natural feasible result: {best['best_distance']:.2f} km with {best['routes']:.0f} routes")
    print(f"Gap to professor reference:  {gap:+.2f} km")
    print("No vehicle target was forced; only TW-feasible SA runs were considered.")


if __name__ == "__main__":
    main()

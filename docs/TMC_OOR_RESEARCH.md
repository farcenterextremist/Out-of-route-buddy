# TMC Transportation — OOR Research Deep Dive

**Research Date:** 2026-03-31
**Duration:** ~1 hour auto research loop
**Purpose:** Ground OutOfRouteBuddy in real-world data from TMC Transportation

---

## 1. Company Profile

| Fact | Detail |
|------|--------|
| **Full Name** | TMC Transportation |
| **HQ** | Des Moines, Iowa (50-acre campus) |
| **Founded** | 1972 by Harrold Annett (purchased The Mickow Corporation) |
| **Fleet** | ~2,000 company trucks, ~3,000 flatbed trailers |
| **Trailer Types** | Standard flatbed, step-deck, RGN, Conestoga, boat trailers, stake trailers |
| **Coverage** | 48 contiguous states, Mexico, Canada |
| **Terminals** | 4 terminals + 12 logistics offices |
| **Ownership** | Employee-owned (ESOP since 2013) |
| **Claim-Free Rate** | 99.7% delivery record |
| **Distinction** | Largest privately-held flatbed carrier in the U.S. |

TMC is **not** a dry van carrier — they are exclusively open-deck/flatbed. This matters for OOR because flatbed routing has unique constraints (load height, weight distribution, oversize permits, tarping stops).

---

## 2. How TMC Pays Drivers (Critical for OOR Context)

### Percentage Pay System (chosen by 98-99% of drivers)

TMC's pay system is **performance-based percentage of the load revenue**, not cents-per-mile. This is the single most important thing to understand about OOR at TMC:

| Level | Percentage | Approx. $/Mile (all miles) |
|-------|-----------|---------------------------|
| New driver minimum | 26% | ~$0.95/mi |
| 92% of fleet | 27%+ | ~$0.99/mi |
| 48% of fleet | 30%+ | ~$1.11/mi |
| Maximum achievable | 32% | ~$1.17/mi |

**How it works:**
1. TMC Logistics gets the load from the customer
2. TMC Logistics takes a ~25% cut off the top → what's left is the **"truck rate"**
3. The driver gets their percentage of the truck rate
4. Load details (including truck rate) are visible in the load notes

**Why this matters for OOR:** Under percentage pay, the driver earns the same regardless of actual miles driven. If you're at 30% and the load pays $1,630 for a 290-mile haul (439 total with empty), you get $489. OOR miles don't directly reduce your paycheck on *that load* — but they DO affect your **monthly percentage rate evaluation**.

### Alternative: Mileage Pay (rare, ~1-2% of drivers)

| Tenure | Rate/Mile |
|--------|----------|
| New | $0.30 |
| 6 months | $0.32 |
| 1 year | $0.38 |
| 2 years | $0.39 |
| 5 years | $0.40 |

Under CPM, OOR miles are completely unpaid — you drove them but don't get compensated.

### Supplemental Pay

| Type | Amount |
|------|--------|
| Steel tarp pay | $30/load |
| Lumber tarp pay | $40/load |
| Stop pay | Yes (amount varies) |
| Detention pay | Yes |
| Breakdown pay | Yes |
| Borough pay | Yes |
| Over-dimensional | Yes |
| Toll & scale passes | Covered |

---

## 3. TMC's OOR Point System (How OOR Affects Your Rate)

**This is the key mechanism.** TMC evaluates drivers monthly on a **points-based scorecard** that determines your percentage rate. OOR is one of the primary factors.

### Monthly Evaluation Factors

| Factor | What Earns Max Points | Impact |
|--------|----------------------|--------|
| **Out of Route Miles** | **< 4% extra miles** | Primary factor |
| **Truck Revenue** | More loads hauled | Primary factor |
| **Fuel Economy (MPG)** | Better than fleet average | Primary factor |
| **On-Time Delivery** | Consistent on-time | Primary factor |
| **Idle Time** | Lower idle | Secondary factor |
| **Sunday Departures** | +5 points per Sunday departure | Can bump rate +1% alone |
| **HAZMAT/TWIC/Passport** | Having certifications | Bonus points |
| **Time with company** | +1 point per year tenure | Loyalty points |
| **Clean truck** | Good presentation | Minor points |
| **TQM class completion** | +5 points | Training bonus |

### OOR Thresholds at TMC

| OOR Level | Meaning |
|-----------|---------|
| **< 4%** | **Gold standard — earns maximum points** |
| **< 10%** | Normal for newer drivers |
| **10-25%** | Some loads legitimately hit this due to circumstances |
| **> 25%** | Red flag, needs explanation |

### Driver Manager Structure

- Each **Fleet Manager** manages **35-40 drivers**
- They conduct **"driver route and hold meetings"** to review performance
- OOR is tracked alongside fuel mileage, idle time, and productivity
- Drivers report that TMC "micromanages like no other" — they want you parked as close to the receiver as possible, leaving on Sunday to pre-position

---

## 4. Real OOR Numbers from TMC Drivers

From TruckersReport forum data and driver reviews:

| Metric | Value | Source |
|--------|-------|--------|
| Driver-reported OOR% | 4.8% (considered "problematic") | TruckersReport forum |
| Industry average OOR% | ~4.34% | Per Diem Plus |
| Annual cost at $0.42/mi CPM | ~$1,956/year for 4.34% OOR | Per Diem Plus |
| Annual cost for owner-ops | ~$5,350/year at $1.15/mi | Per Diem Plus |
| Authorized OOR for fuel/services | Up to 75 miles per detour | Driver forum (military base example) |

### Common OOR Causes at TMC

1. **Fuel stops** — TMC has authorized fuel networks; going to an off-network stop adds OOR
2. **Truck parking shortage** — Drivers spend ~50 min/day searching for parking; 1 spot per 11 drivers nationally
3. **Flatbed-specific detours** — Low bridges (13'6" clearance limits), weight restrictions, oversize permit routes
4. **Construction zones** — Active construction can invalidate planned routes
5. **Pre-positioning** — TMC wants drivers parked at/near receivers; detours to get close add miles
6. **Tarping/securement stops** — Need to find safe locations to tarp/re-secure loads
7. **Weigh stations** — Mandatory stops and occasional bypass routing

### What Drivers Wish They Had

From forum analysis and app reviews:
- **Real-time OOR tracking** — Know your current OOR% during the trip, not after settlement
- **Better notifications** — TMC Driver Connect app has notification reliability issues
- **Settlement auditing** — Per Diem Plus recommends weekly audit of dispatched vs actual miles
- **Fuel stop routing** — Drivers want to see their position relative to authorized fuel stops
- **Historical OOR trends** — See if you're improving or getting worse over time

---

## 5. TMC's Technology Stack

### TMC Driver Connect App

| Feature | Available |
|---------|----------|
| Dispatch load information | Yes |
| Fuel and wash maps | Yes |
| Messaging | Yes |
| Dashboards | Yes |
| News and videos | Yes |
| Paperwork scanning | Yes |
| Company contacts & to-do | Yes |
| Dark mode | Yes (added 2025) |
| Geofencing | Beta (2025) |
| **Real-time OOR tracking** | **No** |
| **OOR analytics/trends** | **No** |
| **Settlement OOR audit** | **No** |

**Key gap:** TMC's own app does NOT provide real-time OOR tracking or analytics. This is the exact gap OutOfRouteBuddy fills.

### ELD/Telematics

TMC uses fleet-grade ELD systems (industry-standard Qualcomm/Omnitracs/PeopleNet ecosystem). These track location, speed, idle time, and HOS but the OOR data they generate is aggregated by fleet managers, not surfaced to drivers in real-time.

---

## 6. Flatbed-Specific OOR Challenges

Flatbed carriers have **inherently higher OOR potential** than dry van carriers:

| Challenge | Why It Adds OOR | Flatbed vs Dry Van |
|-----------|----------------|-------------------|
| **Height restrictions** | Must avoid 13'6" bridges with tall loads | Flatbed loads vary in height; dry van is fixed |
| **Weight distribution** | Steel coils, machinery concentrate weight on specific axles | Palletized freight distributes evenly |
| **Oversize permits** | Must follow state-mandated routes, often longer | Dry van rarely carries oversize |
| **Tarping stops** | Need safe pull-off locations for 30-60 min tarping | No tarping for dry van |
| **Load securement checks** | DOT requires re-checks within 50 miles and every 150 miles | Interior van freight doesn't shift the same way |
| **Conestoga trailers** | Different routing than standard flatbed due to height | Unique to flatbed |
| **Step-deck/RGN** | Lower ground clearance = different route requirements | N/A for dry van |
| **Construction materials** | Often delivered to job sites off major routes | Warehouse docks are on-route |

---

## 7. TMC's ESOP and Why It Matters for App Adoption

TMC became **employee-owned** in 2013. This matters because:

- **Drivers are part-owners** — They have financial incentive to improve fleet efficiency
- **ESOP vests over 6 years** — Encourages retention and engagement with company tools
- **ESOP value = ~$0.055/mile equivalent** placed in retirement (2014 figure)
- **Lower turnover** — Drivers who stay longer have more OOR data history to analyze

This means TMC drivers are more likely to use tools that help them improve their performance metrics (including OOR) because it directly affects both their percentage pay AND their retirement value.

---

## 8. Practical Miles vs HHG Miles (How "Dispatched Miles" Are Calculated)

This is critical because OOR% = (actual - dispatched) / dispatched. How "dispatched" is defined determines OOR%.

| Method | Lost Miles/Year | What It Means |
|--------|----------------|---------------|
| **Practical Miles** | ~3% | Closest to real driving; TMC likely uses this |
| **PC*Miler Short** | ~6% | Middle ground |
| **HHG (Household Goods)** | 5-12% | Worst for drivers; ~10,000-12,000 unpaid miles/year |

**Example:** A 500-mile load:
- Practical miles: 500 dispatched, ~515 actual = 3% OOR
- HHG miles: 470 dispatched, ~515 actual = 9.6% OOR (same trip!)

TMC uses PC*Miler for routing. The dispatched miles shown in load notes are what your OOR% is measured against.

---

## 9. Actionable Insights for OutOfRouteBuddy

### High-Priority Features for TMC Drivers

| Feature | Why TMC Drivers Need It | Priority |
|---------|------------------------|----------|
| **Real-time OOR% display during trip** | TMC's own app doesn't show this; drivers only find out after settlement | P0 |
| **Monthly OOR trend tracking** | Directly maps to TMC's monthly pay rate evaluation | P0 |
| **< 4% target indicator** | TMC's gold standard for max points; show a gauge | P0 |
| **Per-trip OOR breakdown** | Drivers need to see which trips push them over | P1 |
| **Settlement audit helper** | Compare dispatched miles from load notes vs actual driven | P1 |
| **Fuel stop routing impact** | Show how much OOR a fuel detour adds | P2 |
| **Flatbed-aware routing notes** | Flag low bridges, weight restrictions | P2 |
| **Sunday departure tracker** | TMC gives 5 points per Sunday departure | P2 |
| **Tarp stop logger** | Log where you tarped and time spent (explains OOR) | P3 |

### Virtual Fleet Tuning for TMC

Based on this research, the virtual fleet archetypes should be updated:

| Archetype | TMC Reality |
|-----------|-------------|
| **The Veteran** | TMC driver at 30%+ for years; OOR consistently < 3% |
| **The Rookie** | TMC training program graduate, first 6 months; OOR 6-10% |
| **The Efficient** | TMC driver who maxes all evaluation factors; OOR < 2% |
| **The Detour King** | TMC driver with frequent OOR due to flatbed routing; OOR 10-20% |
| **The Night Owl** | TMC night/Sunday departure driver; moderate OOR |
| **The Local** | TMC dedicated route driver (home daily); short runs |
| **The Highway Warrior** | TMC long-haul flatbed; 500-800 mile runs; OOR 1-4% |
| **The Gig Runner** | Less relevant for TMC (company drivers, not gig) — could represent multi-stop lumber yard runs |

### Rankings Scoring Adjustments for TMC

| Factor | Current Weight | TMC-Tuned Weight | Reasoning |
|--------|---------------|-------------------|-----------|
| OOR Efficiency | 40% | 45% | OOR is the primary controllable factor at TMC |
| Consistency | 25% | 25% | TMC evaluates monthly trends, consistency matters |
| Trip Volume | 15% | 10% | TMC assigns loads; volume isn't fully driver-controlled |
| GPS Quality | 10% | 5% | TMC has fleet ELDs; GPS quality is less variable |
| Route Discipline | 10% | 15% | TMC's 4% threshold is the key target |

### Cohort Tuning for TMC

| Cohort | TMC Context |
|--------|------------|
| **Newcomer (1-10 trips)** | First 2 weeks after training; getting used to flatbed OOR |
| **Regular (11-30 trips)** | First 1-3 months; still building evaluation history |
| **Experienced (31-75 trips)** | 3-6 months; established pattern, working toward 30% rate |
| **Legend (76+ trips)** | 6+ months; aiming for max 32% rate, sub-4% OOR consistently |

---

## 10. Competitive Landscape

### What TMC Drivers Use Today

| Tool | What It Does | OOR Gap |
|------|-------------|---------|
| **TMC Driver Connect** | Dispatch, messaging, fuel maps | No OOR tracking/analytics |
| **Per Diem Plus** ($7.99/mo) | Per diem tracking, mileage logging | Basic OOR comparison, not real-time |
| **OTR On The Revenue** | Settlement accounting | No OOR-specific features |
| **DriverAI** ($7.99/mo) | Automatic mileage tracking | Generic; not OOR-focused |
| **MileTrack** (free) | Auto trip detection | No OOR calculation |

**OutOfRouteBuddy's unique value:** The ONLY app focused specifically on real-time OOR% tracking with GPS, trend analytics, and fleet ranking context. No competitor does this.

---

## 11. Key Quotes from TMC Drivers

> "Don't be afraid to ask your dispatcher for out of route miles when it is needed, such as fuel, food, truck washes, etc." — TMC driver, TruckersReport

> "I have an OOR percentage of 4.8%... no out of route fuel" — TMC driver discussing evaluation concerns

> "TMC micromanages like no other. They want you parked up as close to the receiver as possible." — Former TMC driver

> "Less miles, more money. Dollars, not distance. That's TMC percentage pay!" — TMC corporate communications

> "Senseless policies such as fuel mileage 'certification,' out of route, park-ups, dress code..." — TMC driver review

---

## Sources

- tmctrans.com (official company site, pay page, ESOP page, news)
- TruckersReport.com forums (multiple TMC-specific threads)
- Per Diem Plus (perdiemplus.com) — OOR miles cost analysis
- ATRI (American Transportation Research Institute) — operational cost data
- Apple App Store / Google Play — TMC Driver Connect app reviews
- OTrucking.com, Melton Truck Lines — flatbed routing guides
- ClassADrivers.com — TMC driver experience threads
- HiringDriversNow.com — TMC ESOP analysis

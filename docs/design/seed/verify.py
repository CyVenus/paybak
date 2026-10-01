#!/usr/bin/env python3
"""Reference implementation of the Paybak domain (domain.md) and the Figma checks.

Loads demo.json with the clock pinned to the Figma moment (Wed 30 Sep 2026, 21:15 local),
runs the scheduler, computes every read model the screens show and asserts the Figma numbers.
Section numbers (§) point at domain.md. Plain Python 3.9+, no dependencies.

    python3 verify.py            # all checks
"""
from __future__ import annotations

import copy
import datetime as dt
import json
import re
from dataclasses import dataclass, field
from decimal import ROUND_HALF_UP, Decimal
from pathlib import Path

ME = "me"
FIGMA_DAY = dt.date(2026, 9, 30)
FIGMA_NOW = dt.datetime(2026, 9, 30, 21, 15)

# =============================================================================================
# §2 Currencies and money formatting

CURRENCIES = {  # code: (symbol, minor-unit exponent, sentence-case name)
    "INR": ("₹", 2, "Indian rupee"), "AED": ("AED", 2, "UAE dirham"), "USD": ("$", 2, "US dollar"),
    "EUR": ("€", 2, "Euro"), "GBP": ("£", 2, "British pound"), "SGD": ("S$", 2, "Singapore dollar"),
    "AUD": ("A$", 2, "Australian dollar"), "CAD": ("C$", 2, "Canadian dollar"), "JPY": ("¥", 0, "Japanese yen"),
}
MINUS = "−"


def group_digits(whole: int, code: str) -> str:
    digits = str(whole)
    if code != "INR" or len(digits) <= 3:
        return f"{whole:,}"
    head, tail = digits[:-3], digits[-3:]
    pairs = []
    while len(head) > 2:
        pairs.insert(0, head[-2:])
        head = head[:-2]
    return ",".join([head] + pairs + [tail])


def money(minor: int, code: str = "INR", sign: str = "none") -> str:
    """₹2,900 · ₹1,00,000 · ₹1,234.50 · AED 1,800 · +₹2,900 · −₹1,850 (§2.3)."""
    symbol, exponent, _ = CURRENCIES[code]
    value = abs(minor)
    whole, fraction = divmod(value, 10 ** exponent) if exponent else (value, 0)
    text = group_digits(whole, code)
    if fraction:
        text += "." + str(fraction).zfill(exponent)
    body = f"{symbol}{text}" if len(symbol) <= 2 else f"{symbol} {text}"
    if sign == "signed" and minor > 0:
        return "+" + body
    if sign in ("signed", "debit") and minor < 0:
        return MINUS + body
    return body


def convert(minor: int, rate: str, from_code: str, to_code: str) -> int:
    """Amount in `from_code` minor units → `to_code` minor units at a saved decimal rate (half up)."""
    shift = CURRENCIES[to_code][1] - CURRENCIES[from_code][1]
    value = Decimal(minor) * Decimal(rate) * (Decimal(10) ** shift)
    return int(value.quantize(Decimal(1), rounding=ROUND_HALF_UP))


def to_default(minor: int, currency: str, rate: dict | None, default: str) -> int:
    if currency == default:
        return minor
    assert rate and rate["to"] == default, "a foreign amount needs a rate saved to the default currency"
    return convert(minor, rate["value"], currency, default)


# =============================================================================================
# §3 Dates and relative formatting

def fmt_day(d: dt.date) -> str:           # "Mon 28 Sep"
    return f"{d:%a} {d.day} {d:%b}"


def fmt_short(d: dt.date) -> str:          # "26 Sep"
    return f"{d.day} {d:%b}"


def fmt_time(t: dt.datetime) -> str:       # "9:12 pm"
    return f"{t.hour % 12 or 12}:{t:%M} {'am' if t.hour < 12 else 'pm'}"


def row_date(d: dt.date, today: dt.date) -> str:
    """List rows: Today · Yesterday · 26 Sep · 26 Sep 2025."""
    if d == today:
        return "Today"
    if d == today - dt.timedelta(days=1):
        return "Yesterday"
    return fmt_short(d) if d.year == today.year else f"{fmt_short(d)} {d.year}"


def day_header(d: dt.date, today: dt.date) -> str:
    """Timeline day groups: Today · Yesterday · Mon 28 Sep."""
    if d in (today, today - dt.timedelta(days=1)):
        return row_date(d, today)
    return fmt_day(d) if d.year == today.year else f"{fmt_day(d)} {d.year}"


def due_badge(due: dt.date, today: dt.date) -> str:
    """Due Fri (within 6 days) · Due 12 Oct · Overdue 3 days · Overdue 1 day."""
    days = (due - today).days
    if days < 0:
        return f"Overdue {-days} day" + ("" if days == -1 else "s")
    return f"Due {due:%a}" if days <= 6 else f"Due {fmt_short(due)}"


def loan_meta_date(d: dt.date, today: dt.date) -> str:
    age = (today - d).days
    if age in (0, 1):
        return row_date(d, today)
    text = fmt_day(d) if age < 60 else fmt_short(d)
    return text if d.year == today.year else f"{text} {d.year}"


def date_range(start: dt.date, end: dt.date) -> str:
    """21–25 Sep · 28 Sep – 2 Oct."""
    if start == end:
        return fmt_short(start)
    if (start.year, start.month) == (end.year, end.month):
        return f"{start.day}–{end.day} {end:%b}"
    return f"{fmt_short(start)} – {fmt_short(end)}"


def add_months(d: dt.date, months: int, day: int) -> dt.date:
    """Same day of month `months` later, clamped to the month's last day."""
    year, month = divmod(d.month - 1 + months, 12)
    year, month = d.year + year, month + 1
    last = (dt.date(year + month // 12, month % 12 + 1, 1) - dt.timedelta(days=1)).day
    return dt.date(year, month, min(day, last))


def last_day_of_month(d: dt.date) -> dt.date:
    return add_months(d.replace(day=1), 1, 1) - dt.timedelta(days=1)


# =============================================================================================
# §7.1 Loading demo.json: "D±n" and "D±nTHH:MM" resolve against the load day

RELATIVE = re.compile(r"^D([+-]?\d+)(?:T(\d\d):(\d\d))?$")


def resolve(value, anchor: dt.date, now: dt.datetime | None = None):
    """Dates resolve to the load day ± n. Timestamps are moments that already happened, so they
    clamp to `now` (loaded at 9 am, tonight's 19:40 dinner is created "now" instead)."""
    if isinstance(value, dict):
        return {k: resolve(v, anchor, now) for k, v in value.items()}
    if isinstance(value, list):
        return [resolve(v, anchor, now) for v in value]
    if isinstance(value, str) and (m := RELATIVE.match(value)):
        day = anchor + dt.timedelta(days=int(m.group(1)))
        if not m.group(2):
            return day
        moment = dt.datetime(day.year, day.month, day.day, int(m.group(2)), int(m.group(3)))
        return min(moment, now) if now else moment
    return value


# =============================================================================================
# §4 Splits and rounding

def rotate_extra(order: list[str], extra: int, counter: int) -> tuple[dict[str, int], int]:
    """Give `extra` single minor units to people in `order`, starting at counter mod n (§4.1)."""
    bonus = {p: 0 for p in order}
    for i in range(extra):
        bonus[order[(counter + i) % len(order)]] += 1
    return bonus, counter + extra


def split_equal(total: int, order: list[str], counter: int = 0) -> tuple[dict[str, int], int]:
    base, extra = divmod(total, len(order))
    bonus, counter = rotate_extra(order, extra, counter)
    return {p: base + bonus[p] for p in order}, counter


def split_weighted(total: int, weights: dict[str, int], order: list[str], counter: int = 0):
    """Percent (basis points) and Shares: floor, then largest remainder, ties by fair rotation."""
    weight_sum = sum(weights[p] for p in order)
    floors = {p: total * weights[p] // weight_sum for p in order}
    remainders = {p: total * weights[p] % weight_sum for p in order}
    extra = total - sum(floors.values())
    start = counter % len(order)
    rotated = order[start:] + order[:start]
    ranked = sorted(rotated, key=lambda p: -remainders[p])  # stable: rotation breaks ties
    for p in ranked[:extra]:
        floors[p] += 1
    return floors, counter + extra


def split_exact_status(total: int, amounts: dict[str, int]) -> tuple[int, str, str]:
    """(remaining, footer left, footer detail) for the Exact editor (§4.2)."""
    entered = sum(amounts.values())
    remaining = total - entered
    left = f"{money(remaining)} left" if remaining >= 0 else f"{money(-remaining)} over"
    return remaining, left, f"{money(entered)} of {money(total)}"


def largest_remainder_percent(values: dict[str, int]) -> dict[str, int]:
    """Whole percentages that add up to 100 (Insights captions, §6.4)."""
    total = sum(values.values())
    floors = {k: v * 100 // total for k, v in values.items()}
    rema = {k: v * 100 % total for k, v in values.items()}
    for k in sorted(values, key=lambda k: -rema[k])[: 100 - sum(floors.values())]:
        floors[k] += 1
    return floors


def itemized(items: list[tuple[str, int, list[str]]], total: int, order: list[str], counter: int = 0):
    """Receipt split: items evenly among their people, tax and tip in proportion (§4.3)."""
    subtotals = {p: 0 for p in order}
    for _, price, people in items:
        shares, counter = split_equal(price, people, counter)
        for p, s in shares.items():
            subtotals[p] += s
    return split_weighted(total, subtotals, order, counter)[0], subtotals


# =============================================================================================
# The ledger

@dataclass
class Obligation:
    """One open debt between the user and a friend (§5.5)."""
    debtor: str
    creditor: str
    amount: int              # default-currency minor units, > 0
    due: dt.date | None
    title: str               # expense title, group name or loan reason
    kind: str                # direct | group | project | loan
    ref: str                 # expense id, group id or loan id
    installment: int | None = None

    @property
    def friend(self) -> str:
        return self.creditor if self.debtor == ME else self.debtor


@dataclass
class Context:
    """Everything between the user and one friend in one place: a group, a project, a loan or
    the direct (no group) ledger. amount > 0: the friend owes the user."""
    friend: str
    kind: str
    ref: str
    title: str
    amount: int
    items: list[Obligation] = field(default_factory=list)


class Ledger:
    def __init__(self, data: dict, anchor: dt.date, now: dt.datetime):
        self.raw = copy.deepcopy(data)
        d = resolve({k: v for k, v in data.items() if k != "scenarios"}, anchor, now)
        self.anchor, self.now = anchor, now
        self.profile, self.settings = d["profile"], d["settings"]
        self.people = {p["id"]: p for p in d["people"]}
        self.groups = {g["id"]: g for g in d["groups"]}
        self.expenses = {e["id"]: e for e in d["expenses"]}
        self.payments = {p["id"]: p for p in d["payments"]}
        self.loans = {l["id"]: l for l in d["loans"]}
        self.components = {c["id"]: c for c in d["components"]}
        self.rules = {r["id"]: r for r in d["recurringRules"]}
        self.drafts = {x["id"]: x for x in d["drafts"]}
        self.reminders = list(d["reminders"])
        self.notifications = list(d["inbox"])
        self.rotation = dict(d["rotation"])
        self.cursor = d["scheduler"]["cursor"]
        self.scenarios = data["scenarios"]
        self.default = self.profile["currencyCode"]  # the profile currency is the default

    # ---- names ---------------------------------------------------------------------------
    def first(self, pid: str) -> str:
        return "You" if pid == ME else self.people[pid]["name"].split()[0]

    @property
    def today(self) -> dt.date:
        return self.now.date()

    # ---- record filters ("as of" = only what existed at that moment, §5.1) ---------------
    def live_expenses(self, asof: dt.datetime | None = None):
        asof = asof or self.now
        return [e for e in self.expenses.values()
                if e["createdAt"] <= asof and not (e["deletedAt"] and e["deletedAt"] <= asof)]

    def confirmed_payments(self, asof: dt.datetime | None = None):
        asof = asof or self.now
        return [p for p in self.payments.values()
                if p["status"] == "confirmed" and p["confirmedAt"] <= asof]

    # ---- §5.2 group and project nets (in the group's currency) ---------------------------
    def group_nets(self, gid: str, asof=None) -> dict[str, int]:
        g = self.groups[gid]
        nets = {m: 0 for m in g["memberIds"]}
        if g["kind"] == "project":
            paid, share = self.project_paid_share(gid)
            nets = {m: paid[m] - share[m] for m in g["memberIds"]}
        else:
            for e in self.live_expenses(asof):
                if e["groupId"] == gid:
                    for payer in e["payers"]:
                        nets[payer["personId"]] += payer["amount"]
                    for row in e["split"]["rows"]:
                        nets[row["personId"]] -= row["share"]
        for p in self.confirmed_payments(asof):
            if p["groupId"] == gid:
                nets[p["fromId"]] += p["amount"]
                nets[p["toId"]] -= p["amount"]
        return nets

    def group_paid_share(self, gid: str) -> tuple[dict, dict]:
        g = self.groups[gid]
        paid, share = {m: 0 for m in g["memberIds"]}, {m: 0 for m in g["memberIds"]}
        for e in self.live_expenses():
            if e["groupId"] == gid:
                for payer in e["payers"]:
                    paid[payer["personId"]] += payer["amount"]
                for row in e["split"]["rows"]:
                    share[row["personId"]] += row["share"]
        return paid, share

    def group_plan(self, gid: str, asof=None) -> list[tuple[str, str, int]]:
        g = self.groups[gid]
        nets = self.group_nets(gid, asof)
        if g["simplifyDebts"] or g["kind"] == "project":
            return simplify(nets, g["memberIds"])
        return self.pairwise_plan(gid, asof)

    def pairwise_plan(self, gid: str, asof=None) -> list[tuple[str, str, int]]:
        """Simplify off: each participant owes each payer pro rata, netted per pair (§5.3)."""
        pair: dict[tuple[str, str], int] = {}

        def add(debtor, creditor, amount):
            a, b = sorted((debtor, creditor))
            pair[(a, b)] = pair.get((a, b), 0) + (amount if debtor == a else -amount)

        for e in self.live_expenses(asof):
            if e["groupId"] != gid:
                continue
            for row in e["split"]["rows"]:
                for payer in e["payers"]:
                    if payer["personId"] != row["personId"]:
                        owed = row["share"] * payer["amount"] // e["amount"]
                        add(row["personId"], payer["personId"], owed)
        for p in self.confirmed_payments(asof):
            if p["groupId"] == gid:
                add(p["toId"], p["fromId"], p["amount"])
        plan = []
        for (a, b), amount in pair.items():
            if amount > 0:
                plan.append((a, b, amount))
            elif amount < 0:
                plan.append((b, a, -amount))
        return plan

    def group_rate(self, gid: str) -> dict | None:
        """The saved rate of the group's latest record: converts an open foreign-currency group
        balance into the default currency (§5.4)."""
        g = self.groups[gid]
        if g["currency"] == self.default:
            return None
        records = [e for e in self.live_expenses() if e["groupId"] == gid and e["rate"]]
        records += [p for p in self.confirmed_payments() if p["groupId"] == gid and p["rate"]]
        return max(records, key=lambda r: r["createdAt"])["rate"]

    # ---- §8 projects ------------------------------------------------------------------------
    def project_parts(self, gid: str):
        return [c for c in self.components.values() if c["projectId"] == gid]

    def project_spent(self, gid: str) -> int:
        return sum(c["actualCost"] for c in self.project_parts(gid) if c["status"] in ("bought", "done"))

    def project_paid_share(self, gid: str) -> tuple[dict, dict]:
        g = self.groups[gid]
        members = g["memberIds"]
        paid = {m: 0 for m in members}
        for c in self.project_parts(gid):
            if c["status"] in ("bought", "done"):
                paid[c["paidBy"]] += c["actualCost"]
        spent = sum(paid.values())
        rule = g["project"]["contribution"]
        counter = self.rotation.get(gid, 0)
        if rule["rule"] == "equal":
            share, _ = split_equal(spent, members, counter)
        elif rule["rule"] == "percent":
            share, _ = split_weighted(spent, rule["values"], members, counter)
        else:  # fixed: proportional to each member's fixed amount (§8.2)
            share, _ = split_weighted(spent, rule["values"], members, counter)
        return paid, share

    # ---- §5.5 contexts between the user and each friend ----------------------------------
    def contexts(self, asof=None) -> list[Context]:
        result: list[Context] = []
        for gid, g in self.groups.items():
            if ME not in g["memberIds"]:
                continue
            rate = self.group_rate(gid)
            for debtor, creditor, amount in self.group_plan(gid, asof):
                if ME not in (debtor, creditor):
                    continue
                value = to_default(amount, g["currency"], rate, self.default)
                friend = creditor if debtor == ME else debtor
                due = g["settleBy"] if g["kind"] == "group" else None
                ob = Obligation(debtor, creditor, value, due, g["name"], g["kind"], gid)
                result.append(Context(friend, g["kind"], gid, g["name"],
                                      value if creditor == ME else -value, [ob]))
        for friend in self.people:
            direct = self.direct_context(friend, asof)
            if direct:
                result.append(direct)
        for loan in self.loans.values():
            ctx = self.loan_context(loan, asof)
            if ctx:
                result.append(ctx)
        return result

    def direct_context(self, friend: str, asof=None) -> Context | None:
        """Expenses outside any group plus direct payments. Payments pay the oldest debts first,
        so the open items are the newest ones (§5.5)."""
        debts: list[Obligation] = []
        net = 0
        for e in sorted(self.live_expenses(asof), key=lambda e: (e["date"], e["createdAt"])):
            if e["groupId"] is not None:
                continue
            amount = pair_debt(e, friend, ME)  # > 0: friend owes me
            if amount == 0:
                continue
            value = to_default(abs(amount), e["currency"], e["rate"], self.default)
            debtor, creditor = (friend, ME) if amount > 0 else (ME, friend)
            debts.append(Obligation(debtor, creditor, value, e["dueDate"], e["title"], "direct", e["id"]))
            net += value if amount > 0 else -value
        for p in self.confirmed_payments(asof):
            if p["groupId"] or p["loanId"] or {p["fromId"], p["toId"]} != {ME, friend}:
                continue
            value = to_default(p["amount"], p["currency"], p["rate"], self.default)
            net += -value if p["fromId"] == friend else value
        if not debts and net == 0:
            return None
        items, left = [], abs(net)
        for ob in reversed(debts):  # newest first
            if left == 0:
                break
            if (ob.creditor == ME) == (net > 0):
                take = min(left, ob.amount)
                items.append(Obligation(ob.debtor, ob.creditor, take, ob.due, ob.title, "direct", ob.ref))
                left -= take
        return Context(friend, "direct", friend, "", net, items)

    def loan_context(self, loan: dict, asof=None) -> Context | None:
        friend = loan["borrowerId"] if loan["lenderId"] == ME else loan["lenderId"]
        if (asof or self.now) < loan["createdAt"]:
            return None
        paid = sum(p["amount"] for p in self.confirmed_payments(asof) if p["loanId"] == loan["id"])
        remaining = loan["amount"] - paid
        items = []
        for number, (due, amount, paid_on) in enumerate(self.installments(loan, asof), start=1):
            if paid_on is None:
                items.append(Obligation(loan["borrowerId"], loan["lenderId"], amount, due,
                                        loan["reason"] or "Loan", "loan", loan["id"], number))
        sign = 1 if loan["lenderId"] == ME else -1
        return Context(friend, "loan", loan["id"], loan["reason"] or "Loan", sign * remaining, items)

    def installments(self, loan: dict, asof=None) -> list[tuple[dt.date, int, dt.date | None]]:
        """(due, amount, paid date) per installment; repayments fill them in due order (§9)."""
        plan = loan["installments"]
        if plan:
            count = plan["count"]
            amounts, _ = split_equal(loan["amount"], list(range(count)))  # leftover to the earliest
            dues = [installment_due(plan["firstDue"], plan["frequency"], i) for i in range(count)]
        else:
            amounts, dues, count = {0: loan["amount"]}, [loan["dueDate"]], 1
        repayments = sorted((p for p in self.confirmed_payments(asof) if p["loanId"] == loan["id"]),
                            key=lambda p: p["date"])
        result, pool, queue = [], 0, list(repayments)
        for i in range(count):
            need, paid_on = amounts[i], None
            while pool < need and queue:
                pay = queue.pop(0)
                pool += pay["amount"]
                paid_on = pay["date"]
            if pool >= need:
                pool -= need
                result.append((dues[i], amounts[i], paid_on or result[-1][2]))
            else:
                result.append((dues[i], amounts[i], None))
        return result

    def friend_nets(self, asof=None) -> dict[str, int]:
        nets = {pid: 0 for pid in self.people}
        for ctx in self.contexts(asof):
            nets[ctx.friend] += ctx.amount
        return nets

    def open_items(self, asof=None) -> list[Obligation]:
        """Obligations in the direction of each friend's overall net (§5.6)."""
        nets = self.friend_nets(asof)
        items = []
        for ctx in self.contexts(asof):
            for ob in ctx.items:
                owed_to_me = ob.creditor == ME
                if (nets[ctx.friend] > 0 and owed_to_me) or (nets[ctx.friend] < 0 and not owed_to_me):
                    items.append(ob)
        return items

    # ---- §6 read models ----------------------------------------------------------------------
    def home_totals(self, asof=None) -> dict:
        nets = self.friend_nets(asof)
        owed = {p: v for p, v in nets.items() if v > 0}
        owe = {p: v for p, v in nets.items() if v < 0}
        groups, people = set(), set()
        for ob in self.open_items(asof):
            if ob.debtor == ME:
                (groups if ob.kind in ("group", "project") else people).add(ob.ref if ob.kind in ("group", "project") else ob.friend)
        return dict(owed=sum(owed.values()), owedPeople=len(owed), owe=-sum(owe.values()),
                    oweGroups=len(groups), owePeople=len(people))

    def owe_caption(self, totals: dict) -> str:
        g, p = totals["oweGroups"], totals["owePeople"]
        groups = f"{g} group" + ("" if g == 1 else "s")
        people = f"{p} " + ("person" if p == 1 else "people")
        if g and p:
            return f"across {groups} and {people}"
        return f"across {groups}" if g else f"to {people}"

    def settle_rows(self) -> tuple[list, list]:
        """Settle up (§6.3): one row per friend, payments to make and people who owe you."""
        nets = self.friend_nets()
        items = self.open_items()
        rows = []
        for friend, net in nets.items():
            if net == 0:
                continue
            mine = [ob for ob in items if ob.friend == friend]
            dated = sorted((ob for ob in mine if ob.due), key=lambda ob: ob.due)
            lead = dated[0] if dated else (mine[0] if mine else None)
            rows.append(dict(friend=friend, amount=abs(net), pay=net < 0,
                             context=lead.title if lead else "", due=lead.due if lead else None))
        order = list(self.people)

        def key(r):
            overdue = r["due"] is not None and r["due"] < self.today
            return (not overdue, r["due"] or dt.date.max, order.index(r["friend"]))
        pay = sorted((r for r in rows if r["pay"]), key=key)
        get = sorted((r for r in rows if not r["pay"]), key=key)
        return pay, get

    def due_soon(self) -> list[dict]:
        """Home Due soon (§6.2): overdue or due within 2 days; your group debts show the group."""
        rows = []
        for ob in self.open_items():
            if ob.due is None or ob.due > self.today + dt.timedelta(days=2):
                continue
            if ob.debtor == ME and ob.kind == "group":
                rows.append(dict(title=self.groups[ob.ref]["name"], detail="Your share", amount=ob.amount,
                                 badge=due_badge(ob.due, self.today), action="Settle", due=ob.due))
            else:
                rows.append(dict(title=self.first(ob.friend), detail=ob.title, amount=ob.amount,
                                 badge=due_badge(ob.due, self.today),
                                 action="Remind" if ob.creditor == ME else "Settle", due=ob.due))
        return sorted(rows, key=lambda r: r["due"])[:3]

    def my_share(self, e: dict) -> int:
        return next((r["share"] for r in e["split"]["rows"] if r["personId"] == ME), 0)

    def i_owe_payer(self, e: dict) -> bool:
        """'You owe ₹450' when the payer is who you pay in that context, else 'Your share' (§6.6)."""
        payer = e["payers"][0]["personId"]
        if e["groupId"] is None:
            return any(ob.debtor == ME and ob.ref == e["id"] for ob in self.open_items())
        return any(d == ME and c == payer for d, c, _ in self.group_plan(e["groupId"]))

    # ---- §6.6 activity (derived) -------------------------------------------------------------
    def timeline(self) -> list[dict]:
        events = []
        for e in self.expenses.values():
            g = self.groups.get(e["groupId"]) if e["groupId"] else None
            involved = any(r["personId"] == ME for r in e["split"]["rows"]) or (g and ME in g["memberIds"])
            if not involved:
                continue
            payer = e["payers"][0]["personId"]
            total = e["amount"]
            if e["deletedAt"] is None:
                actor = e["createdBy"]
                if payer == ME:
                    sub = f"You paid · {len(e['split']['rows'])} people" if not g else f"{g['name']} · You paid"
                else:
                    word = "You owe" if self.i_owe_payer(e) else "Your share"
                    sub = f"{g['name'] if g else self.first(payer)} · {word} {money(self.my_share(e), e['currency'])}"
                title = f"You added {e['title']}" if actor == ME else f"{self.first(actor)} added {e['title']}"
                events.append(dict(at=e["createdAt"], kind="expenseAdded", title=title, subtitle=sub,
                                   amount=money(total, e["currency"]), primary=payer == ME, home=True, ref=e["id"]))
            for h in e["history"]:
                if h["kind"] == "amountChanged":
                    events.append(dict(at=h["at"], kind="expenseEdited",
                                       title=f"{self.first(h['by'])} changed {e['title']}",
                                       subtitle=f"{g['name'] if g else ''} · Was {money(h['old'], e['currency'])}",
                                       amount=money(total, e["currency"]), primary=payer == ME, home=False, ref=e["id"]))
        for p in self.payments.values():
            if ME in (p["fromId"], p["toId"]) and p["status"] == "confirmed":
                other = p["toId"] if p["fromId"] == ME else p["fromId"]
                what = self.payment_for(p)
                title = f"{self.first(other)} paid you" if p["toId"] == ME else f"You paid {self.first(other)}"
                events.append(dict(at=p["confirmedAt"], kind="payment", title=title,
                                   subtitle=f"{what} · {METHODS[p['method']]} · Confirmed",
                                   amount=money(p["amount"], p["currency"]), primary=p["toId"] == ME,
                                   home=True, method=METHODS[p["method"]], ref=p["id"]))
        for r in self.reminders:
            if r["fromId"] == ME:
                what = self.expenses[r["expenseId"]]["title"] if r["expenseId"] else (
                    self.groups[r["groupId"]]["name"] if r["groupId"] else self.loans[r["loanId"]]["reason"])
                how = "Sent automatically" if r["automatic"] else "Sent by you"
                events.append(dict(at=r["sentAt"], kind="reminderSent", title=f"Reminder sent to {self.first(r['toId'])}",
                                   subtitle=f"{what} · {money(r['amount'], r['currency'])} · {how}",
                                   amount=None, home=False, ref=r["id"]))
        for d in self.drafts.values():
            rule = self.rules[d["ruleId"]]
            events.append(dict(at=d["createdAt"], kind="draftCreated", title=f"{rule['title']} draft created",
                               subtitle=f"{self.groups[rule['groupId']]['name']} · Needs an amount",
                               amount=None, badge="Draft", home=False, ref=d["id"]))
        for loan in self.loans.values():
            other = loan["borrowerId"] if loan["lenderId"] == ME else loan["lenderId"]
            events.append(dict(at=loan["createdAt"], kind="loanAdded",
                               title=f"You lent {self.first(other)}" if loan["lenderId"] == ME else f"{self.first(other)} lent you",
                               subtitle=loan["reason"] or "Loan", amount=money(loan["amount"], loan["currency"]),
                               primary=loan["lenderId"] == ME, home=False, ref=loan["id"]))
        events = [e for e in events if e["at"] <= self.now]
        return sorted(events, key=lambda e: e["at"], reverse=True)

    def payment_for(self, p: dict) -> str:
        if p["expenseId"]:
            return self.expenses[p["expenseId"]]["title"]
        if p["groupId"]:
            return self.groups[p["groupId"]]["name"]
        if p["loanId"]:
            return self.loans[p["loanId"]]["reason"]
        return "Payment"

    def pending_claims(self) -> list[dict]:
        """Payments friends recorded to you that wait for your Confirm (§6.7), newest first."""
        return sorted((p for p in self.payments.values() if p["toId"] == ME and p["status"] == "pending"),
                      key=lambda p: p["createdAt"], reverse=True)

    def claim_card(self, p: dict) -> tuple[str, str]:
        payer = self.people[p["fromId"]]
        pronoun = payer.get("pronoun") or "they"
        return (f"{self.first(p['fromId'])} says {pronoun} paid you {money(p['amount'], p['currency'])}",
                f"{self.payment_for(p)} · {METHODS[p['method']]} · {fmt_time(p['createdAt'])}")

    def inbox(self) -> list[dict]:
        rows = []
        for n in self.notifications:
            rows.append(dict(at=n["createdAt"], type=n["type"], read=n["read"], **notification_text(self, n)))
        return sorted(rows, key=lambda r: r["at"], reverse=True)

    # ---- §6.4 insights -----------------------------------------------------------------------
    def insight_expenses(self, month: tuple[int, int], asof=None):
        """Your share of group and friend expenses dated in `month`; projects are separate."""
        for e in self.live_expenses(asof):
            if (e["date"].year, e["date"].month) != month or self.my_share(e) == 0:
                continue
            if e["groupId"] and self.groups[e["groupId"]]["kind"] == "project":
                continue
            yield e, to_default(self.my_share(e), e["currency"], e["rate"], self.default)

    def insights(self, month: tuple[int, int], asof=None) -> dict:
        by_category, by_group, total = {}, {}, 0
        for e, share in self.insight_expenses(month, asof):
            total += share
            by_category[e["category"]] = by_category.get(e["category"], 0) + share
            key = self.groups[e["groupId"]]["name"] if e["groupId"] else "Without a group"
            by_group[key] = by_group.get(key, 0) + share
        return dict(total=total, categories=by_category, groups=by_group)


METHODS = {"cash": "Cash", "upi": "UPI", "bank": "Bank", "card": "Card", "other": "Other"}
CATEGORIES = {  # id: (name, icon), in picker order (§1.9)
    "food": ("Food", "food"), "travel": ("Travel", "car"), "stays": ("Stays", "bed"), "fun": ("Fun", "ticket"),
    "rent": ("Rent", "home"), "bills": ("Bills", "bolt"), "shopping": ("Shopping", "shopping-bag"),
    "other": ("Other", "tag"),
}


def pair_debt(e: dict, a: str, b: str) -> int:
    """What `a` owes `b` on one expense (negative: b owes a); several payers count pro rata."""
    share = {r["personId"]: r["share"] for r in e["split"]["rows"]}
    paid = {p["personId"]: p["amount"] for p in e["payers"]}
    total = e["amount"]
    a_owes = share.get(a, 0) * paid.get(b, 0) // total
    b_owes = share.get(b, 0) * paid.get(a, 0) // total
    return a_owes - b_owes


def simplify(nets: dict[str, int], order: list[str]) -> list[tuple[str, str, int]]:
    """Fewest transfers: largest debtor pays largest creditor; ties by member order (§5.3)."""
    nets = dict(nets)
    transfers = []
    while True:
        debtors = [p for p in order if nets[p] < 0]
        creditors = [p for p in order if nets[p] > 0]
        if not debtors or not creditors:
            return transfers
        debtor = min(debtors, key=lambda p: (nets[p], order.index(p)))
        creditor = min(creditors, key=lambda p: (-nets[p], order.index(p)))
        amount = min(-nets[debtor], nets[creditor])
        transfers.append((debtor, creditor, amount))
        nets[debtor] += amount
        nets[creditor] -= amount


def installment_due(first: dt.date, frequency: str, index: int) -> dt.date:
    if frequency == "weekly":
        return first + dt.timedelta(weeks=index)
    if frequency == "biweekly":
        return first + dt.timedelta(weeks=2 * index)
    return add_months(first, index, first.day)


# =============================================================================================
# §6.8 notification copy (from the snapshot params)

def notification_text(ledger: Ledger, n: dict) -> dict:
    p, t = n["params"], n["type"]
    if t == "newExpenseInGroup":
        return dict(title=f"New expense in {ledger.groups[p['groupId']]['name']}",
                    body=f"{ledger.first(p['actorId'])} added {p['title']}, {money(p['total'], p['currency'])}. "
                         f"Your share is {money(p['share'], p['currency'])}.")
    if t == "paymentOverdue":
        return dict(title="Payment overdue",
                    body=f"{ledger.first(p['personId'])} owes you {money(p['amount'], p['currency'])} for {p['title']}. "
                         f"It was due on {fmt_short(p['dueDate'])}.")
    if t == "paymentConfirmed":
        return dict(title="Payment confirmed",
                    body=f"{ledger.first(p['personId'])} paid you {money(p['amount'], p['currency'])} for {p['title']} "
                         f"by {METHODS[p['method']]}.")
    if t == "paymentReminder":
        days = (p["dueDate"] - n["createdAt"].date()).days
        when = ("It’s due today." if days == 0 else "It’s due tomorrow." if days == 1 else
                f"It’s due {p['dueDate']:%A}." if 0 < days <= 6 else
                f"It’s due on {fmt_short(p['dueDate'])}." if days > 6 else f"It was due on {fmt_short(p['dueDate'])}.")
        return dict(title="Payment reminder",
                    body=f"You owe {ledger.first(p['personId'])} {money(p['amount'], p['currency'])} for {p['title']}. {when}")
    if t == "monthlySummary":
        month = dt.date(p["year"], p["month"], 1)
        tail = (f"You’re owed {money(p['owed'])}." if p["owed"] else
                f"You owe {money(p['owe'])}." if p["owe"] else "You’re all square.")
        return dict(title="Monthly summary",
                    body=f"{month:%B}: you spent {money(p['spent'])} on shared expenses. {tail}")
    raise ValueError(t)


# =============================================================================================
# §10 The scheduler: reminders, overdue alerts, monthly summaries, recurring occurrences

def fires_on(due: dt.date, day: dt.date, schedule: dict) -> bool:
    if schedule["twoDaysBefore"] and day == due - dt.timedelta(days=2):
        return True
    if schedule["onDueDate"] and day == due:
        return True
    return schedule["overdueEvery3Days"] and day > due and (day - due).days % 3 == 0


def next_occurrence(rule: dict, after: dt.date) -> dt.date:
    anchor = rule["anchorDate"]
    if rule["frequency"] == "weekly":
        return after + dt.timedelta(days=(anchor.weekday() - after.weekday() - 1) % 7 + 1)
    if rule["frequency"] == "yearly":
        candidate = anchor.replace(year=after.year)
        return candidate if candidate > after else anchor.replace(year=after.year + 1)
    candidate = add_months(after.replace(day=1), 0, anchor.day)
    return candidate if candidate > after else add_months(after.replace(day=1), 1, anchor.day)


def run_scheduler(ledger: Ledger, until: dt.datetime) -> None:
    schedule = ledger.settings["reminderSchedule"]
    hour, minute = map(int, schedule["time"].split(":"))
    day = ledger.cursor.date()
    while day <= until.date():
        jobs = [(dt.time(9, 0), recurring_job), (dt.time(9, 0), overdue_job),
                (dt.time(20, 0), summary_job), (dt.time(hour, minute), reminder_job)]
        for at, job in jobs:
            moment = dt.datetime.combine(day, at)
            if ledger.cursor < moment <= until:
                job(ledger, moment)
        day += dt.timedelta(days=1)
    for eid, e in list(ledger.expenses.items()):  # Recently deleted keeps 30 days
        if e["deletedAt"] and e["deletedAt"] + dt.timedelta(days=30) <= until:
            del ledger.expenses[eid]
    for gid, g in ledger.groups.items():          # a closed project archives once everyone is square
        if g["project"] and g["project"]["status"] == "closed" and not any(ledger.group_nets(gid, until).values()):
            g["project"].update(status="archived", archivedAt=until)
    ledger.cursor = until


def recurring_job(ledger: Ledger, moment: dt.datetime) -> None:
    for rule in ledger.rules.values():
        if not rule["active"]:
            continue
        occurrence = next_occurrence(rule, rule["lastOccurrence"])
        if occurrence != moment.date():
            continue
        rule["lastOccurrence"] = occurrence
        if rule["variable"]:
            did = f"d-{rule['id']}-{occurrence}"
            ledger.drafts[did] = dict(id=did, ruleId=rule["id"], occurrenceDate=occurrence, createdAt=moment, expenseId=None)
        else:
            order = rule["split"]["personIds"]
            counter = ledger.rotation.get(rule["groupId"], 0)
            shares, ledger.rotation[rule["groupId"]] = split_equal(rule["amount"], order, counter)
            eid = f"e-{rule['id']}-{occurrence}"
            ledger.expenses[eid] = dict(
                id=eid, groupId=rule["groupId"], title=rule["title"], category=rule["category"], amount=rule["amount"],
                currency=rule["currency"], rate=None, date=occurrence, dueDate=None,
                payers=[dict(personId=rule["payerId"], amount=rule["amount"])],
                split=dict(mode="equal", rows=[dict(personId=p, included=True, value=None, share=shares[p]) for p in order]),
                itemized=None, notes=None, receipt=None, recurringRuleId=rule["id"], occurrenceDate=occurrence,
                createdAt=moment, createdBy=rule["createdBy"], history=[dict(kind="created", at=moment, by=rule["createdBy"])],
                comments=[], flag=None, deletedAt=None, deletedBy=None)


def overdue_job(ledger: Ledger, moment: dt.datetime) -> None:
    for ob in ledger.open_items(moment):
        if ob.creditor == ME and ob.due and ob.due == moment.date() - dt.timedelta(days=1):
            ledger.notifications.append(dict(
                id=f"n-overdue-{ob.ref}-{moment:%Y%m%d}", type="paymentOverdue", createdAt=moment, read=False,
                params=dict(personId=ob.friend, amount=ob.amount, currency=ledger.default, title=ob.title,
                            expenseId=ob.ref if ob.kind == "direct" else None, groupId=None, loanId=None, dueDate=ob.due)))


def summary_job(ledger: Ledger, moment: dt.datetime) -> None:
    if moment.date() != last_day_of_month(moment.date()):
        return
    report = ledger.insights((moment.year, moment.month), moment)
    totals = ledger.home_totals(moment)
    ledger.notifications.append(dict(
        id=f"n-summary-{moment:%Y%m}", type="monthlySummary", createdAt=moment, read=False,
        params=dict(year=moment.year, month=moment.month, spent=report["total"], owed=totals["owed"], owe=totals["owe"])))


def reminder_job(ledger: Ledger, moment: dt.datetime) -> None:
    schedule = ledger.settings["reminderSchedule"]
    for ob in ledger.open_items(moment):
        if not ob.due or not fires_on(ob.due, moment.date(), schedule):
            continue
        if ob.creditor == ME:
            if ledger.people[ob.friend]["remindersMuted"]:
                continue
            ledger.reminders.append(dict(
                id=f"rem-{ob.ref}-{moment:%Y%m%d}", toId=ob.friend, fromId=ME, amount=ob.amount, currency=ledger.default,
                expenseId=ob.ref if ob.kind == "direct" else None, groupId=ob.ref if ob.kind == "group" else None,
                loanId=ob.ref if ob.kind == "loan" else None, installment=ob.installment, sentAt=moment,
                automatic=True, message=None))
        else:
            ledger.notifications.append(dict(
                id=f"n-reminder-{ob.ref}-{moment:%Y%m%d}", type="paymentReminder", createdAt=moment, read=False,
                params=dict(personId=ob.friend, amount=ob.amount, currency=ledger.default, title=ob.title,
                            dueDate=ob.due, groupId=ob.ref if ob.kind == "group" else None)))


# =============================================================================================
# §7.4 Store actions used by the scenarios

def apply_scenario(ledger: Ledger, name: str) -> None:
    for step in ledger.scenarios[name]:
        if "use" in step:
            apply_scenario(ledger, step["use"])
            continue
        # Scenario moments clamp to now like seeded ones; setClock is the one step that moves time.
        step = resolve(step, ledger.anchor, None if step["action"] == "setClock" else ledger.now)
        at = step.get("at")
        if at and at > ledger.cursor:
            run_scheduler(ledger, at)
        ACTIONS[step["action"]](ledger, step)


def act_record_payment(ledger, step):
    p = dict(step["payment"], rate=None, note=None, proof=None, status="pending",
             createdAt=step["at"], confirmedAt=None, notReceivedNote=None)
    ledger.payments[p["id"]] = p


def act_confirm_payment(ledger, step):
    p = ledger.payments[step["paymentId"]]
    p.update(status="confirmed", confirmedAt=step["at"])
    if p["toId"] == ME:
        ledger.notifications.append(dict(id=f"n-confirmed-{p['id']}", type="paymentConfirmed", createdAt=step["at"],
                                         read=True, params=dict(paymentId=p["id"], personId=p["fromId"], amount=p["amount"],
                                                                currency=p["currency"], title=ledger.payment_for(p),
                                                                method=p["method"])))


def act_not_received(ledger, step):
    ledger.payments[step["paymentId"]].update(status="notReceived", notReceivedNote=step["note"])


def act_flag(ledger, step):
    e = ledger.expenses[step["expenseId"]]
    e["flag"] = dict(by=step["by"], note=step["note"], at=step["at"])
    e["history"].append(dict(kind="flagged", at=step["at"], by=step["by"]))


def act_add_loan(ledger, step):
    ledger.loans[step["loan"]["id"]] = dict(step["loan"], rate=None, createdAt=step["at"], createdBy=ME)


def act_update_component(ledger, step):
    c = ledger.components[step["componentId"]]
    c.update(status=step["status"], actualCost=step["actualCost"], paidBy=step["paidBy"], statusChangedAt=step["at"])


def act_close_project(ledger, step):
    g = ledger.groups[step["projectId"]]
    g["project"].update(status="closed", closedAt=step["at"])
    if not any(ledger.group_nets(g["id"]).values()):
        g["project"].update(status="archived", archivedAt=step["at"])


def act_create_group(ledger, step):
    ledger.groups[step["group"]["id"]] = dict(step["group"], createdAt=step["at"], createdBy=ME, project=None)


def act_set_clock(ledger, step):
    run_scheduler(ledger, step["at"])
    ledger.now = step["at"]


def act_set_entitlement(ledger, step):
    ledger.settings["entitlement"] = dict(plan=step["plan"], period=step["period"], trialEndsAt=step["trialEndsAt"], since=ledger.now)


def act_clear(ledger, step):
    for name in ("people", "groups", "expenses", "payments", "loans", "components", "rules", "drafts"):
        setattr(ledger, name, {})
    ledger.reminders, ledger.notifications = [], []


ACTIONS = {
    "recordPayment": act_record_payment, "confirmPayment": act_confirm_payment,
    "markNotReceived": act_not_received, "flagExpense": act_flag, "addLoan": act_add_loan,
    "updateComponent": act_update_component, "closeProject": act_close_project,
    "createGroup": act_create_group, "setClock": act_set_clock, "setEntitlement": act_set_entitlement,
    "clearLedger": act_clear,
}


# =============================================================================================
# Checks

DEMO = json.loads(Path(__file__).with_name("demo.json").read_text())
checks = 0


def check(actual, expected, what: str) -> None:
    global checks
    assert actual == expected, f"{what}: expected {expected!r}, got {actual!r}"
    checks += 1


def load(*scenarios: str, now: dt.datetime = FIGMA_NOW, anchor: dt.date = FIGMA_DAY) -> Ledger:
    ledger = Ledger(DEMO, anchor, now)
    run_scheduler(ledger, ledger.now)
    for name in scenarios:
        apply_scenario(ledger, name)
    return ledger


def rupee(x: float) -> int:
    return round(x * 100)


def check_formatting():
    check(money(rupee(2900), sign="signed"), "+₹2,900", "owed amount")
    check(money(-rupee(1850), sign="signed"), "−₹1,850", "owe amount (U+2212)")
    check(money(rupee(100000)), "₹1,00,000", "Indian grouping, lakh")
    check(money(rupee(9999999.5)), "₹99,99,999.50", "Indian grouping with paise")
    check(money(rupee(1800), "AED"), "AED 1,800", "AED amount")
    check(money(rupee(1200), "EUR"), "€1,200", "short symbol")
    check(money(1234, "JPY"), "¥1,234", "zero-decimal currency")
    check(convert(rupee(960), "22.85", "AED", "INR"), rupee(21936), "AED 960 at 22.85")
    check(row_date(dt.date(2026, 9, 26), FIGMA_DAY), "26 Sep", "row date")
    check(day_header(dt.date(2026, 9, 28), FIGMA_DAY), "Mon 28 Sep", "day header")
    check(due_badge(dt.date(2026, 10, 2), FIGMA_DAY), "Due Fri", "due badge")
    check(due_badge(dt.date(2026, 9, 27), FIGMA_DAY), "Overdue 3 days", "overdue badge")
    check(fmt_time(dt.datetime(2026, 9, 30, 21, 12)), "9:12 pm", "claim time")
    check(date_range(dt.date(2026, 9, 21), dt.date(2026, 9, 25)), "21–25 Sep", "trip range")
    check(loan_meta_date(dt.date(2026, 6, 12), FIGMA_DAY), "12 Jun", "loan meta (old)")
    check(loan_meta_date(FIGMA_DAY, dt.date(2026, 11, 3)), "Wed 30 Sep", "loan meta (34 days)")


def check_splits():
    shares, counter = split_equal(rupee(2800), ["me", "priya", "esha", "dev"])
    check(set(shares.values()), {rupee(700)}, "₹2,800 ÷ 4")
    shares, counter = split_equal(rupee(1000), ["me", "priya", "esha"], 0)
    check(shares, {"me": 33334, "priya": 33333, "esha": 33333}, "₹1,000 ÷ 3, counter 0")
    shares, counter = split_equal(rupee(1000), ["me", "priya", "esha"], counter)
    check(shares, {"me": 33333, "priya": 33334, "esha": 33333}, "the next ₹1,000 ÷ 3 rotates")
    check(split_exact_status(rupee(2800), {"me": rupee(700), "p": rupee(700), "e": rupee(700), "d": rupee(550)}),
          (rupee(150), "₹150 left", "₹2,650 of ₹2,800"), "Exact footer (06-06)")
    shares, _ = split_weighted(rupee(1000), {"a": 3333, "b": 3333, "c": 3334}, ["a", "b", "c"])
    check(sum(shares.values()), rupee(1000), "percent split adds up")
    shares, _ = split_weighted(rupee(2800), {"a": 2, "b": 1, "c": 1}, ["a", "b", "c"])
    check(shares, {"a": rupee(1400), "b": rupee(700), "c": rupee(700)}, "shares 2:1:1")
    items = [("Chicken biryani", rupee(430), ["dev"]), ("Paneer tikka", rupee(370), ["esha"]),
             ("Fish and chips", rupee(450), ["me"]), ("Chocolate brownie", rupee(240), ["me"]),
             ("Masala fries", rupee(240), ["me", "esha", "dev"]), ("Fresh lime soda ×3", rupee(270), ["me", "esha", "dev"])]
    totals, subtotals = itemized(items, rupee(2300), ["me", "esha", "dev"])
    check(subtotals, {"me": rupee(860), "esha": rupee(540), "dev": rupee(600)}, "receipt item subtotals")
    check(totals, {"me": rupee(989), "esha": rupee(621), "dev": rupee(690)}, "receipt totals with GST and tip")


def check_home_and_friends():
    ledger = load()
    totals = ledger.home_totals()
    check(money(totals["owed"], sign="signed"), "+₹2,900", "Home You’re owed")
    check(f"from {totals['owedPeople']} people", "from 4 people", "Home owed caption")
    check(money(-totals["owe"], sign="signed"), "−₹1,850", "Home You owe")
    check(ledger.owe_caption(totals), "across 2 groups", "Home owe caption")
    nets = ledger.friend_nets()
    check({ledger.first(p): v // 100 for p, v in nets.items()},
          {"Rohan": 800, "Priya": 700, "Esha": 700, "Dev": 700, "Kabir": -1400, "Meera": -450, "Ananya": 0},
          "friend nets (07-02)")
    due = [(r["title"], r["detail"], r["amount"] // 100, r["badge"], r["action"]) for r in ledger.due_soon()]
    check(due, [("Rohan", "Movie tickets", 800, "Overdue 3 days", "Remind"),
                ("Goa Trip", "Your share", 1400, "Due Fri", "Settle")], "Home Due soon")
    recent = [e for e in ledger.timeline() if e["home"]][:3]
    check([(e["title"] if e["kind"] == "payment" else ledger.expenses[e["ref"]]["title"]) for e in recent],
          ["Dinner at Olive Garden", "Priya paid you", "Electricity bill"], "Home Recent activity rows")
    check([row_date(e["at"].date(), ledger.today) for e in recent], ["Today", "Yesterday", "26 Sep"], "Recent dates")
    elec = ledger.expenses["e-flat-elec-09"]
    check(f"Flat 302 · {'You owe' if ledger.i_owe_payer(elec) else 'Your share'}", "Flat 302 · You owe", "Recent row 3 subtitle")
    check(money(-ledger.my_share(elec), sign="signed"), "−₹450", "Recent row 3 amount")

    # Friends list order (§6.5): overdue owed, owed by due, owe by due, then no balance
    items = ledger.open_items()

    def row_key(pid):
        net = nets[pid]
        mine = sorted((ob.due for ob in items if ob.friend == pid and ob.due), key=lambda d: d)
        due = mine[0] if mine else dt.date.max
        bucket = 0 if net > 0 and due < ledger.today else 1 if net > 0 else 2 if net < 0 else 3
        return (bucket, due if bucket < 3 else dt.date.max, -abs(net), list(ledger.people).index(pid))
    order = sorted(ledger.people, key=row_key)
    check([ledger.first(p) for p in order], ["Rohan", "Priya", "Esha", "Dev", "Kabir", "Meera", "Ananya"], "Friends order")

    pay, get = ledger.settle_rows()
    check([(ledger.first(r["friend"]), r["amount"] // 100, r["context"], due_badge(r["due"], ledger.today)) for r in pay],
          [("Kabir", 1400, "Goa Trip", "Due Fri"), ("Meera", 450, "Flat 302", "Due Mon")], "Settle up: 2 payments to make")
    check([(ledger.first(r["friend"]), r["amount"] // 100, r["context"], due_badge(r["due"], ledger.today)) for r in get],
          [("Rohan", 800, "Movie tickets", "Overdue 3 days"), ("Priya", 700, "Dinner at Olive Garden", "Due Sun"),
           ("Esha", 700, "Dinner at Olive Garden", "Due Sun"), ("Dev", 700, "Dinner at Olive Garden", "Due Sun")],
          "Settle up: 4 people owe you")
    check([fmt_day(r["due"]) for r in pay], ["Fri 2 Oct", "Mon 5 Oct"], "owe breakdown due labels")
    check(ledger.owe_caption(totals).replace("across ", ""), "2 groups", "owe breakdown hero count")
    check(f"You still owe {money(totals['owe'])} and are owed {money(totals['owed'])}.",
          "You still owe ₹1,850 and are owed ₹2,900.", "Delete account blocked (12-10)")

    # Ask Paybak "Who owes me money?" (§6.9)
    check(ask_who_owes(ledger), "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and "
          "Dev ₹700 each for tonight’s dinner.", "Ask Paybak answer")
    goa = next(ob for ob in items if ob.kind == "group" and ob.ref == "g-goa")
    check(f"Your Goa Trip share of {money(goa.amount)} is due {fmt_day(goa.due)}.",
          "Your Goa Trip share of ₹1,400 is due Fri 2 Oct.", "Ask Paybak: When is Goa Trip due?")


def ask_who_owes(ledger: Ledger) -> str:
    nets = ledger.friend_nets()
    items = ledger.open_items()
    pay, get = ledger.settle_rows()
    parts, grouped = [], {}
    for row in get:
        mine = [ob for ob in items if ob.friend == row["friend"]]
        overdue = [ob for ob in mine if ob.due and ob.due < ledger.today]
        if overdue:
            parts.append(f"{ledger.first(row['friend'])} {money(row['amount'])} (overdue since {fmt_short(overdue[0].due)})")
        elif len(mine) == 1 and mine[0].kind == "direct":
            grouped.setdefault((mine[0].ref, row["amount"]), []).append(ledger.first(row["friend"]))
        else:
            parts.append(f"{ledger.first(row['friend'])} {money(row['amount'])}")
    for (eid, amount), names in grouped.items():
        e = ledger.expenses[eid]
        if e["date"] == ledger.today and e["category"] == "food" and e["title"].startswith("Dinner"):
            what = "tonight’s dinner"
        elif e["date"] == ledger.today:
            what = f"today’s {e['title'].lower()}"
        else:
            what = e["title"]
        who = names[0] if len(names) == 1 else ", ".join(names[:-1]) + " and " + names[-1]
        parts.append(f"{who} {money(amount)}{' each' if len(names) > 1 else ''} for {what}")
    total = sum(v for v in nets.values() if v > 0)
    count = sum(1 for v in nets.values() if v > 0)
    joined = parts[0] if len(parts) == 1 else ", ".join(parts[:-1]) + ", and " + parts[-1]
    return f"{count} people owe you {money(total)}: {joined}."


def check_groups():
    ledger = load()
    paid, share = ledger.group_paid_share("g-goa")
    nets = ledger.group_nets("g-goa")
    check(sum(paid.values()) // 100, 39500, "Goa Trip spent")
    check({ledger.first(m): (paid[m] // 100, share[m] // 100, nets[m] // 100) for m in nets},
          {"You": (6500, 7900, -1400), "Kabir": (18000, 7900, 10100), "Priya": (3500, 7900, -4400),
           "Esha": (5000, 7900, -2900), "Dev": (6500, 7900, -1400)}, "Goa Trip paid vs share")
    dates = [e["date"] for e in ledger.live_expenses() if e["groupId"] == "g-goa"]
    check(f"{date_range(min(dates), max(dates))} · 5 members · {money(sum(paid.values()))} spent",
          "21–25 Sep · 5 members · ₹39,500 spent", "Goa Trip title row")
    plan = ledger.group_plan("g-goa")
    check(sorted((ledger.first(d), ledger.first(c), a // 100) for d, c, a in plan),
          sorted([("You", "Kabir", 1400), ("Priya", "Kabir", 4400), ("Esha", "Kabir", 2900), ("Dev", "Kabir", 1400)]),
          "Goa Trip simplified plan")
    debtors = [m for m in ledger.groups["g-goa"]["memberIds"] if any(d == m for d, _, _ in plan)]
    names = [ledger.first(m) for m in debtors]
    check(f"Simplify debts is on. {', '.join(names[:-1])} and {names[-1]} each pay Kabir directly.",
          "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly.", "Goa Trip footnote")
    check(ledger.expenses["e-goa-snacks"]["deletedAt"] is not None, True, "Snacks is deleted and not counted")
    check(ledger.expenses["e-goa-villa"]["amount"] // 5 // 100, 3600, "Villa: your share")

    # Owe-breakdown footnote (§6.3): your payee isn't who paid the expenses since you were last square
    check(simplified_footnote_groups(ledger), ["Goa Trip"], "You owe footnote groups")

    # Flat 302 and the other rows of the Groups list (07-01)
    check(ledger.group_nets("g-flat302"), {ME: -45000, "p-meera": 90000, "p-kabir": -45000}, "Flat 302 nets")
    check(ledger.group_plan("g-flat302"), [(ME, "p-meera", 45000), ("p-kabir", "p-meera", 45000)], "Flat 302 plan")
    check(set(ledger.group_nets("g-college").values()), {0}, "College Gang settled")
    rules = [r for r in ledger.rules.values() if r["groupId"] == "g-flat302" and r["active"]]
    check(f"{len(rules)} rules", "3 rules", "Flat 302 recurring rules")

    # Dubai Weekend: AED, saved rates, settled
    paid, share = ledger.group_paid_share("g-dubai")
    check({ledger.first(m): (money(paid[m], "AED"), money(share[m], "AED")) for m in paid},
          {"You": ("AED 540", "AED 600"), "Kabir": ("AED 960", "AED 600"), "Meera": ("AED 300", "AED 600")},
          "Dubai paid vs share")
    check(set(ledger.group_nets("g-dubai").values()), {0}, "Dubai settled")
    lines, total_inr = [], 0
    for e in sorted((e for e in ledger.live_expenses() if e["groupId"] == "g-dubai"), key=lambda e: e["date"], reverse=True):
        inr = convert(e["amount"], e["rate"]["value"], "AED", "INR")
        total_inr += inr
        lines.append(f"≈ {money(round(inr / 100) * 100)} · ₹{e['rate']['value']} per AED")
    check(lines, ["≈ ₹6,870 · ₹22.90 per AED", "≈ ₹12,312 · ₹22.80 per AED", "≈ ₹21,936 · ₹22.85 per AED"], "Dubai rate lines")
    check(f"Total {money(sum(paid.values()), 'AED')} · ≈ {money(total_inr)} at saved rates",
          "Total AED 1,800 · ≈ ₹41,118 at saved rates", "Dubai total footnote")
    last = max((p for p in ledger.confirmed_payments() if p["groupId"] == "g-dubai" and ME in (p["fromId"], p["toId"])),
               key=lambda p: p["confirmedAt"])
    check(f"You paid {ledger.first(last['toId'])} {money(last['amount'], 'AED')} on {fmt_short(last['date'])}",
          "You paid Kabir AED 60 on 14 Mar", "Dubai settled caption")

    # Groups list order (§6.5)
    def row(gid):
        g = ledger.groups[gid]
        mine = [ob for ob in ledger.open_items() if ob.ref == gid]
        archived = bool(g["project"]) and g["project"]["status"] == "archived"
        return (archived, not mine, mine[0].due if mine else dt.date.max, g["name"])
    order = sorted((gid for gid, g in ledger.groups.items() if ME in g["memberIds"]), key=row)
    check([ledger.groups[g]["name"] for g in order],
          ["Goa Trip", "Flat 302", "Build a Drone", "College Gang", "Dubai Weekend", "Hackathon Kit"], "Groups list order")
    check([f"{len(ledger.groups[g]['memberIds'])} members" for g in ("g-goa", "g-flat302", "g-college", "g-dubai")],
          ["5 members", "3 members", "6 members", "3 members"], "member counts")

    # Friend — Rohan (07-08)
    rohan = [e for e in ledger.live_expenses() if any(r["personId"] == "p-rohan" for r in e["split"]["rows"])
             and not (e["groupId"] and ledger.groups[e["groupId"]]["kind"] == "project")]
    check([(e["title"], money(e["amount"]), fmt_short(e["date"])) for e in sorted(rohan, key=lambda e: e["date"], reverse=True)],
          [("Movie tickets", "₹1,600", "20 Sep"), ("Farewell dinner", "₹9,000", "12 Mar")], "Rohan history")
    together = [g["name"] for g in ledger.groups.values() if "p-rohan" in g["memberIds"]]
    check(together, ["College Gang", "Build a Drone"], "Rohan groups together")
    last_reminder = max(r["sentAt"] for r in ledger.reminders if r["toId"] == "p-rohan")
    check(last_reminder.date() == ledger.today, True, "Last reminder sent today")
    check(sorted(r["sentAt"].date().day for r in ledger.reminders if r["toId"] == "p-rohan"), [25, 27, 30],
          "Rohan's automatic reminders (Fri 25, Sun 27, Wed 30 Sep)")


def simplified_footnote_groups(ledger: Ledger) -> list[str]:
    """Groups where you pay someone other than the people who paid for what you owe since your
    group balance was last zero (§6.3)."""
    result = []
    for gid, g in ledger.groups.items():
        if not g["simplifyDebts"] or g["kind"] != "group":
            continue
        payees = {c for d, c, _ in ledger.group_plan(gid) if d == ME}
        if not payees:
            continue
        events = [(e["createdAt"], "e", e) for e in ledger.live_expenses() if e["groupId"] == gid]
        events += [(p["confirmedAt"], "p", p) for p in ledger.confirmed_payments() if p["groupId"] == gid]
        running, since_zero = 0, []
        for _, kind, r in sorted(events, key=lambda x: x[0]):
            if kind == "e":
                running += sum(x["amount"] for x in r["payers"] if x["personId"] == ME) - ledger.my_share(r)
                if r["payers"][0]["personId"] != ME and ledger.my_share(r):
                    since_zero.append(r["payers"][0]["personId"])
            else:
                running += r["amount"] if r["fromId"] == ME else -r["amount"] if r["toId"] == ME else 0
            if running == 0:
                since_zero = []
        if set(since_zero) != payees:
            result.append(g["name"])
    return result


def check_projects():
    ledger = load()
    spent = ledger.project_spent("pj-drone")
    budget = ledger.groups["pj-drone"]["project"]["budget"]
    planned = sum(c["estimatedCost"] or 0 for c in ledger.project_parts("pj-drone") if c["status"] == "planned")
    check((money(spent), money(budget)), ("₹52,000", "₹60,000"), "Drone spent of budget")
    check(int(Decimal(spent * 100) / Decimal(budget) + Decimal("0.5")), 87, "Drone % used (half up)")
    check(money(budget - spent), "₹8,000", "Drone left")
    check(f"Planned items bring it to {money(spent + planned)}", "Planned items bring it to ₹58,000", "Drone projection")
    paid, share = ledger.project_paid_share("pj-drone")
    nets = ledger.group_nets("pj-drone")
    check({ledger.first(m): (paid[m] // 100, share[m] // 100, nets[m] // 100) for m in paid},
          {"You": (13000, 13000, 0), "Dev": (25500, 13000, 12500), "Priya": (9000, 13000, -4000),
           "Rohan": (4500, 13000, -8500)}, "Drone paid vs fair share")
    order = sorted(nets, key=lambda m: (-nets[m], ledger.groups["pj-drone"]["memberIds"].index(m)))
    check([ledger.first(m) for m in order], ["Dev", "You", "Priya", "Rohan"], "fair-share row order")
    scale = max(max(paid.values()), max(share.values()))
    check({ledger.first(m): round(paid[m] * 100 / scale) for m in paid}, {"You": 51, "Dev": 100, "Priya": 35, "Rohan": 18},
          "fair-share bar fills (%)")
    plan = sorted(ledger.group_plan("pj-drone"), key=lambda t: -t[2])
    check([f"{ledger.first(d)} owes {ledger.first(c)} {money(a)}" for d, c, a in plan],
          ["Rohan owes Dev ₹8,500", "Priya owes Dev ₹4,000"], "Drone who owes whom")
    listed = sorted(ledger.project_parts("pj-drone"),
                    key=lambda c: (["planned", "bought", "done"].index(c["status"]), -c["statusChangedAt"].timestamp()))
    check([c["name"] for c in listed], ["GPS module", "Camera", "Transmitter", "ESCs and propellers", "Battery",
                                       "Flight controller", "Motors ×4", "Frame"], "Drone component order")
    check(ledger.home_totals()["owed"], rupee(2900), "project debts between others leave Home alone")

    over = load("devBuysGps")
    spent = over.project_spent("pj-drone")
    check(f"{money(spent)} of {money(budget)} · {money(spent - budget)} over budget",
          "₹61,500 of ₹60,000 · ₹1,500 over budget", "over-budget card")
    nets = over.group_nets("pj-drone")
    check({over.first(m): nets[m] / 100 for m in nets}, {"You": -2375, "Dev": 19625, "Priya": -6375, "Rohan": -10875},
          "over-budget nets (₹15,375 each)")

    closed = load("closeDrone")
    check(closed.groups["pj-drone"]["project"]["status"], "closed", "Drone closes (others still owe)")
    check([f"{closed.first(d)} pays {closed.first(c)} {money(a)}" for d, c, a in sorted(closed.group_plan("pj-drone"), key=lambda t: -t[2])],
          ["Rohan pays Dev ₹8,500", "Priya pays Dev ₹4,000"], "final plan")
    check(f"{money(budget - closed.project_spent('pj-drone'))} under budget", "₹8,000 under budget", "closed budget line")
    for pid, frm, amount in [("pay-rohan-dev", "p-rohan", 8500), ("pay-priya-dev", "p-priya", 4000)]:
        act_record_payment(closed, dict(at=closed.now, payment=dict(id=pid, fromId=frm, toId="p-dev", amount=rupee(amount),
                           currency="INR", method="upi", date=closed.today, groupId="pj-drone", loanId=None,
                           expenseId=None, recordedBy=frm)))
        act_confirm_payment(closed, dict(at=closed.now, paymentId=pid))
    run_scheduler(closed, closed.now + dt.timedelta(minutes=1))
    check(closed.groups["pj-drone"]["project"]["status"], "archived", "closed project archives once both are confirmed")

    spent = ledger.project_spent("pj-hackathon")
    budget = ledger.groups["pj-hackathon"]["project"]["budget"]
    check((money(spent), round(spent * 100 / budget), money(budget - spent)), ("₹18,400", 92, "₹1,600"), "Hackathon Kit")
    check(set(ledger.group_nets("pj-hackathon").values()), {0}, "Hackathon Kit everyone settled")
    check(fmt_short(ledger.groups["pj-hackathon"]["project"]["closedAt"].date()), "30 Aug", "Project · Closed 30 Aug")


def check_insights():
    ledger = load()
    sep = ledger.insights((2026, 9))
    check(money(sep["total"]), "₹23,300", "September total")
    cats = sorted(sep["categories"].items(), key=lambda kv: -kv[1])
    pct = largest_remainder_percent(dict(cats))
    check([(CATEGORIES[k][0], pct[k], money(v), round(310 * pct[k] / 100, 1)) for k, v in cats],
          [("Rent", 51, "₹12,000", 158.1), ("Food", 17, "₹3,850", 52.7), ("Stays", 15, "₹3,600", 46.5),
           ("Fun", 8, "₹1,800", 24.8), ("Travel", 5, "₹1,200", 15.5), ("Bills", 4, "₹850", 12.4)], "By category")
    groups = sorted(sep["groups"].items(), key=lambda kv: -kv[1])
    pct = largest_remainder_percent(dict(groups))
    check([(k, pct[k], money(v)) for k, v in groups],
          [("Flat 302", 55, "₹12,850"), ("Goa Trip", 34, "₹7,900"), ("Without a group", 11, "₹2,550")], "Who you spent with")
    months = [(2026, m) for m in range(4, 10)]
    totals = [ledger.insights(m)["total"] for m in months]
    check([t // 100 for t in totals], [18400, 21950, 19600, 20600, 22200, 23300], "six-month totals")
    check([round(120 * t / max(totals)) for t in totals], [95, 113, 101, 106, 114, 120], "bar heights (pt)")
    change = round(abs(totals[-1] - totals[-2]) * 100 / totals[-2])
    check(f"{'Up' if totals[-1] > totals[-2] else 'Down'} {change}% from August", "Up 5% from August", "trend badge")
    lent = [l for l in ledger.loans.values() if l["lenderId"] == ME and l["date"] >= dt.date(2026, 4, 1)]
    borrowed = [l for l in ledger.loans.values() if l["borrowerId"] == ME and l["date"] >= dt.date(2026, 4, 1)]
    check((money(sum(l["amount"] for l in lent)), money(sum(l["amount"] for l in borrowed))), ("₹4,500", "₹0"),
          "Lent vs borrowed since April")
    food = sep["categories"]["food"]
    check(f"You spent {money(food)} on food in September — {largest_remainder_percent(sep['categories'])['food']}% "
          f"of your {money(sep['total'])} share.",
          "You spent ₹3,850 on food in September — 17% of your ₹23,300 share.", "Ask Paybak food answer")


def check_loans():
    ledger = load()
    kabir = ledger.loans["l-kabir-bike"]
    rows = ledger.installments(kabir)
    labels = []
    for due, amount, paid_on in rows:
        late = (paid_on - due).days
        labels.append(f"Paid {fmt_short(paid_on)}" + (f" · {late} day{'s' if late != 1 else ''} late" if late > 0 else ""))
    check(labels, ["Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late"], "Kabir loan rows")
    check(f"{kabir['reason']} · {loan_meta_date(kabir['date'], ledger.today)}", "Bike service · 12 Jun", "Kabir loan meta")
    check(f"Paid back on {fmt_short(rows[-1][2])}", "Paid back on 14 Sep", "Kabir loan caption")

    added = load("lendDev")
    dev = added.loans["l-dev-laptop"]
    rows = added.installments(dev)
    check([(fmt_day(d), money(a)) for d, a, _ in rows],
          [("Fri 30 Oct", "₹2,000"), ("Mon 30 Nov", "₹2,000"), ("Wed 30 Dec", "₹2,000")], "Dev loan schedule")
    check(f"{dev['reason']} · {loan_meta_date(dev['date'], added.today)}", "Laptop repair · Today", "Dev loan meta")
    check(added.friend_nets()["p-dev"], rupee(6700), "a loan counts in the friend net")

    overdue = load("lendDevOverdue")
    check(overdue.now, dt.datetime(2026, 11, 3, 10, 0), "loanOverdue clock (Tue 3 Nov)")
    first_due = overdue.installments(overdue.loans["l-dev-laptop"])[0][0]
    check(due_badge(first_due, overdue.today), "Overdue 4 days", "installment 1 overdue")
    sent = sorted(r["sentAt"] for r in overdue.reminders if r["loanId"] == "l-dev-laptop")
    check([fmt_day(t.date()) for t in sent], ["Wed 28 Oct", "Fri 30 Oct", "Mon 2 Nov"], "loan reminder log")
    check(f"Last reminder sent {fmt_day(sent[-1].date())}", "Last reminder sent Mon 2 Nov", "loan footnote")
    check(f"{dev['reason']} · {loan_meta_date(dev['date'], overdue.today)}", "Laptop repair · Wed 30 Sep", "meta 34 days later")
    check("e-goa-snacks" in overdue.expenses, False, "Snacks purged 30 days after it was deleted (24 Oct)")


def check_activity_and_inbox():
    ledger = load("eshaClaimsPayment")
    claims = ledger.pending_claims()
    check([ledger.claim_card(p) for p in claims],
          [("Esha says she paid you ₹700", "Dinner at Olive Garden · UPI · 9:12 pm")], "Confirm payment card")
    check(ledger.home_totals()["owed"], rupee(2900), "a pending claim changes nothing")
    rows = [(day_header(e["at"].date(), ledger.today), e["title"], e["subtitle"], e.get("amount"))
            for e in ledger.timeline() if e["at"].date() >= dt.date(2026, 9, 25)]
    check(rows, [
        ("Today", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", None),
        ("Today", "You added Dinner at Olive Garden", "You paid · 4 people", "₹2,800"),
        ("Yesterday", "Priya paid you", "Weekend groceries · UPI · Confirmed", "₹1,050"),
        ("Mon 28 Sep", "Kabir changed Villa (3 nights)", "Goa Trip · Was ₹17,500", "₹18,000"),
        ("Mon 28 Sep", "Cooking gas draft created", "Flat 302 · Needs an amount", None),
        ("Sun 27 Sep", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", None),
        ("Sat 26 Sep", "Meera added Electricity bill", "Flat 302 · You owe ₹450", "₹1,350"),
        ("Fri 25 Sep", "Reminder sent to Rohan", "Movie tickets · ₹800 · Sent automatically", None),
        ("Fri 25 Sep", "Dev added Fuel", "Goa Trip · Your share ₹500", "₹2,500"),
    ], "Activity timeline (09-01)")

    inbox = ledger.inbox()
    today = [(r["title"], r["body"], fmt_time(r["at"]), r["read"]) for r in inbox if r["at"].date() == ledger.today]
    check(today, [("Payment reminder", "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.", "9:00 pm", False),
                  ("Monthly summary", "September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900.", "8:00 pm", False)],
          "Notifications: Today")
    earlier = [(r["title"], r["body"], r["read"]) for r in inbox if r["at"].date() < ledger.today][:3]
    check(earlier, [("Payment confirmed", "Priya paid you ₹1,050 for Weekend groceries by UPI.", True),
                    ("Payment overdue", "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep.", True),
                    ("New expense in Flat 302", "Meera added Electricity bill, ₹1,350. Your share is ₹450.", True)],
          "Notifications: Earlier")

    deleted = ledger.expenses["e-goa-snacks"]
    purge = deleted["deletedAt"].date() + dt.timedelta(days=30)
    check(f"{money(deleted['amount'])} · Goa Trip | Deleted by {ledger.first(deleted['deletedBy'])} on "
          f"{fmt_short(deleted['deletedAt'].date())} · {(purge - ledger.today).days} days left",
          "₹300 · Goa Trip | Deleted by Priya on 24 Sep · 24 days left", "Recently deleted row")

    confirmed = load("eshaPaymentConfirmed")
    totals = confirmed.home_totals()
    check((money(totals["owed"], sign="signed"), f"from {totals['owedPeople']} people"), ("+₹2,200", "from 3 people"),
          "Home after Confirm")
    first = [e for e in confirmed.timeline() if e["home"]][0]
    check((first["title"], first["method"], first["amount"], row_date(first["at"].date(), confirmed.today)),
          ("Esha paid you", "UPI", "₹700", "Today"), "new Recent activity row")
    check(confirmed.pending_claims(), [], "the card is gone")

    rejected = load("eshaPaymentNotReceived")
    check((rejected.home_totals()["owed"], rejected.pending_claims()), (rupee(2900), []), "Not received keeps ₹700 owed")


def check_payments():
    pending = load("paymentToMeeraPending")
    check(money(-pending.home_totals()["owe"], sign="signed"), "−₹1,850", "pending payment changes nothing")
    done = load("paymentToMeeraConfirmed")
    totals = done.home_totals()
    check((money(-totals["owe"], sign="signed"), done.owe_caption(totals)), ("−₹1,400", "across 1 group"),
          "after Meera confirms")
    check(load("paymentToKabirPending").home_totals()["owe"], rupee(1850), "Kabir pending: Home keeps −₹1,850")
    settled = load("allSettled")
    check(settled.home_totals(), dict(owed=0, owedPeople=0, owe=0, oweGroups=0, owePeople=0), "Home — All settled")
    flagged = load("eshaFlagsSeafood")
    check(flagged.expenses["e-goa-seafood"]["flag"]["note"], "I left before dessert. Can we check the bill?", "dispute")
    check(flagged.group_nets("g-goa")[ME], -rupee(1400), "a disputed expense still counts")
    empty = load("empty")
    check((empty.home_totals()["owed"], empty.timeline()), (0, []), "empty account")
    trek = load("weekendTrek")
    g = trek.groups["g-trek"]
    check(f"Trip · {len(g['memberIds'])} members · {g['currency']}", "Trip · 4 members · INR", "Group created subtitle")
    check(set(trek.group_nets("g-trek").values()), {0}, "Group created balance ₹0")


def check_recurring_and_misc():
    ledger = load()
    nexts = {r["title"]: fmt_day(next_occurrence(r, ledger.today)) for r in ledger.rules.values()}
    check(nexts, {"Rent": "Thu 1 Oct", "Wi-Fi": "Mon 5 Oct", "Cooking gas": "Wed 28 Oct"}, "recurring next dates")
    draft = next(iter(ledger.drafts.values()))
    check(f"{draft['occurrenceDate']:%B} draft · {fmt_short(draft['occurrenceDate'])}", "September draft · 28 Sep", "draft row")
    check(fmt_day(ledger.today + dt.timedelta(days=7)), "Wed 7 Oct", "trial end (paywall welcome)")
    pro = load("pro")
    check(pro.settings["entitlement"]["plan"], "pro", "Pro scenario")

    # Export records, This month (12-09): a row starts ticked iff it has a record in range
    start, end = dt.date(2026, 9, 1), dt.date(2026, 9, 30)
    ticked = set()
    for e in ledger.live_expenses():
        if start <= e["date"] <= end:
            ticked.add(ledger.groups[e["groupId"]]["name"] if e["groupId"] else "Without a group")
    for p in ledger.confirmed_payments():
        if start <= p["date"] <= end and ME in (p["fromId"], p["toId"]):
            ticked.add(ledger.groups[p["groupId"]]["name"] if p["groupId"] else "Without a group")
    for c in ledger.components.values():
        if start <= c["statusChangedAt"].date() <= end:
            ticked.add(ledger.groups[c["projectId"]]["name"])
    check(ticked, {"Goa Trip", "Flat 302", "Build a Drone", "Without a group"}, "Export default ticks (September)")
    check(f"{start.day} {start:%b} – {end.day} {end:%b %Y}", "1 Sep – 30 Sep 2026", "Export range line")

    # tick is idempotent: running it again at the same moment adds nothing
    counts = (len(ledger.reminders), len(ledger.notifications), len(ledger.expenses), len(ledger.drafts))
    run_scheduler(ledger, ledger.now)
    check((len(ledger.reminders), len(ledger.notifications), len(ledger.expenses), len(ledger.drafts)), counts,
          "tick idempotence")
    # one day later: Rent is added on Thu 1 Oct at 09:00 and the Flat 302 nets move by it
    later = load(now=dt.datetime(2026, 10, 1, 10, 0))
    check(later.expenses["e-r-rent-2026-10-01"]["amount"], rupee(36000), "Rent occurrence generated by tick")
    check(later.group_nets("g-flat302")[ME], rupee(-450 + 24000), "Flat 302 after October rent")

    # Consistency on another day: loading on Thu 1 Oct at 9 am shifts everything by one day
    shifted = load(now=dt.datetime(2026, 10, 1, 9, 0), anchor=dt.date(2026, 10, 1))
    totals = shifted.home_totals()
    check((totals["owed"], totals["owe"]), (rupee(2900), rupee(1850)), "totals on another day")
    check([r["badge"] for r in shifted.due_soon()], ["Overdue 3 days", "Due Sat"], "Due soon on another day")
    check(any(r["sentAt"].date() == shifted.today for r in shifted.reminders), False,
          "today's 9 pm reminder hasn't fired at 9 am")


if __name__ == "__main__":
    for run in (check_formatting, check_splits, check_home_and_friends, check_groups, check_projects,
                check_insights, check_loans, check_activity_and_inbox, check_payments, check_recurring_and_misc):
        run()
        print(f"ok  {run.__name__}")
    print(f"All {checks} checks pass.")

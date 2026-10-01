#!/usr/bin/env python3
"""Builds demo.json, the Paybak demo dataset (domain.md §7).

The records are written here with absolute Figma dates (the anchor "now" is Wed 30 Sep 2026)
and saved with relative dates ("D-9", "D-9T15:30") so the dataset shifts with the day it is
loaded on. The past-month settlement payments are computed, not typed, so every group ends
exactly where Figma says it is.

    python3 build_demo.py && python3 verify.py

demo.json is what the apps load. Edit this file and re-run it rather than editing the JSON.
"""
import datetime as dt
import json
from pathlib import Path

ANCHOR = dt.date(2026, 9, 30)
ME = "me"


def rel(value: str) -> str:
    """'2026-09-21' → 'D-9'; '2026-09-21T15:30' → 'D-9T15:30'."""
    date_part, _, time_part = value.partition("T")
    days = (dt.date.fromisoformat(date_part) - ANCHOR).days
    sign = "+" if days > 0 else ""
    return f"D{sign}{days}" + (f"T{time_part}" if time_part else "")


def rupees(amount: float) -> int:
    """₹ → paise (minor units)."""
    return round(amount * 100)


# ---------------------------------------------------------------------------------------------
# People (seed order = the order friends were added; it breaks ties in the Friends list)

people = [
    dict(id="p-rohan", name="Rohan Verma", avatar="avatar-3", upi="rohan@ybl", username="rohan", pronoun="he"),
    dict(id="p-priya", name="Priya Sharma", avatar="avatar-2", upi="priya@okhdfcbank", username="priya", pronoun="she"),
    dict(id="p-esha", name="Esha Kapoor", avatar="avatar-4", upi="esha@okicici", username="esha", pronoun="she"),
    dict(id="p-dev", name="Dev Malhotra", avatar="avatar-5", upi="dev@oksbi", username="dev", pronoun="he"),
    dict(id="p-kabir", name="Kabir Singh", avatar="avatar-6", upi="kabir@okaxis", username="kabir", pronoun="he"),
    dict(id="p-meera", name="Meera Iyer", avatar="avatar-7", upi="meera@okhdfcbank", username="meera", pronoun="she"),
]
for index, person in enumerate(people):
    person.update(isGuest=False, contact=None, remindersMuted=False, addedAt=rel(f"2026-02-01T10:0{index}"))
people.append(dict(
    id="p-ananya", name="Ananya Rao", avatar=None, upi=None, username=None, pronoun="she",
    isGuest=True, contact="+91 98765 43210", remindersMuted=False, addedAt=rel("2026-09-26T12:00"),
))

# ---------------------------------------------------------------------------------------------
# Groups and projects

GOA, FLAT, COLLEGE, DUBAI = "g-goa", "g-flat302", "g-college", "g-dubai"
DRONE, HACKATHON = "pj-drone", "pj-hackathon"

groups = [
    dict(id=GOA, kind="group", type="trip", icon="plane", name="Goa Trip", currency="INR",
         memberIds=[ME, "p-kabir", "p-priya", "p-esha", "p-dev"], simplifyDebts=True,
         settleBy=rel("2026-10-02"), createdAt=rel("2026-09-10T20:00"), createdBy="p-kabir"),
    dict(id=FLAT, kind="group", type="home", icon="home", name="Flat 302", currency="INR",
         memberIds=[ME, "p-meera", "p-kabir"], simplifyDebts=True,
         settleBy=rel("2026-10-05"), createdAt=rel("2026-03-28T19:00"), createdBy=ME),
    dict(id=COLLEGE, kind="group", type="friends", icon="people", name="College Gang", currency="INR",
         memberIds=[ME, "p-rohan", "p-priya", "p-esha", "p-dev", "p-kabir"], simplifyDebts=True,
         settleBy=None, createdAt=rel("2026-03-01T18:00"), createdBy="p-kabir"),
    dict(id=DUBAI, kind="group", type="trip", icon="plane", name="Dubai Weekend", currency="AED",
         memberIds=[ME, "p-kabir", "p-meera"], simplifyDebts=True,
         settleBy=None, createdAt=rel("2026-03-01T12:00"), createdBy=ME),
    dict(id=DRONE, kind="project", type=None, icon="drone", name="Build a Drone", currency="INR",
         memberIds=[ME, "p-dev", "p-priya", "p-rohan"], simplifyDebts=True, settleBy=None,
         createdAt=rel("2026-08-10T11:00"), createdBy=ME,
         project=dict(description=None, budget=rupees(60000),
                      contribution=dict(rule="equal", values={}), pool=False,
                      status="active", closedAt=None, archivedAt=None)),
    dict(id=HACKATHON, kind="project", type=None, icon="package", name="Hackathon Kit", currency="INR",
         memberIds=[ME, "p-esha", "p-dev", "p-kabir"], simplifyDebts=True, settleBy=None,
         createdAt=rel("2026-08-01T10:00"), createdBy="p-esha",
         project=dict(description=None, budget=rupees(20000),
                      contribution=dict(rule="equal", values={}), pool=False,
                      status="archived", closedAt=rel("2026-08-30T18:00"),
                      archivedAt=rel("2026-08-30T18:00"))),
]
for group in groups:
    group.setdefault("project", None)

# ---------------------------------------------------------------------------------------------
# Expenses

expenses = []


def expense(eid, title, category, amount, date, payer, people_on, *, group=None, due=None,
            created=None, created_by=None, currency="INR", rate=None, rule=None, **extra):
    """An equal split of `amount` (whole units of `currency`) between `people_on`."""
    total = rupees(amount)
    share, left = divmod(total, len(people_on))
    assert left == 0, f"{eid}: the demo only uses splits that divide evenly"
    record = dict(
        id=eid, groupId=group, title=title, category=category, amount=total, currency=currency,
        rate=rate, date=rel(date), dueDate=rel(due) if due else None,
        payers=[dict(personId=payer, amount=total)],
        split=dict(mode="equal", rows=[dict(personId=p, included=True, value=None, share=share) for p in people_on]),
        itemized=None, notes=None, receipt=None,
        recurringRuleId=rule, occurrenceDate=rel(date) if rule else None,
        createdAt=rel(created or f"{date}T20:00"), createdBy=created_by or payer,
        history=[], comments=[], flag=None, deletedAt=None, deletedBy=None,
    )
    record["history"].append(dict(kind="created", at=record["createdAt"], by=record["createdBy"]))
    record.update(extra)
    expenses.append(record)
    return record


GOA_PEOPLE = [ME, "p-kabir", "p-priya", "p-esha", "p-dev"]
villa = expense("e-goa-villa", "Villa (3 nights)", "stays", 18000, "2026-09-21", "p-kabir", GOA_PEOPLE,
                group=GOA, created="2026-09-21T15:30")
villa["receipt"] = dict(asset="receipt-thumb", addedBy="p-kabir", addedAt=rel("2026-09-21T15:30"))
villa["history"].append(dict(kind="amountChanged", at=rel("2026-09-28T18:20"), by="p-kabir",
                             old=rupees(17500), new=rupees(18000)))
villa["comments"] = [
    dict(id="cm-villa-1", by="p-priya", at=rel("2026-09-27T10:15"), text="Was breakfast included?"),
    dict(id="cm-villa-2", by="p-kabir", at=rel("2026-09-28T18:25"), text="Yes, all three days. Updated the total."),
]
expense("e-goa-scooter", "Scooter rentals", "travel", 3500, "2026-09-21", "p-priya", GOA_PEOPLE,
        group=GOA, created="2026-09-21T11:00")
expense("e-goa-seafood", "Seafood dinner at Britto’s", "food", 6500, "2026-09-22", ME, GOA_PEOPLE,
        group=GOA, created="2026-09-22T22:10")
expense("e-goa-parasailing", "Parasailing", "fun", 5000, "2026-09-23", "p-esha", GOA_PEOPLE,
        group=GOA, created="2026-09-23T17:00")
snacks = expense("e-goa-snacks", "Snacks", "food", 300, "2026-09-23", "p-priya", GOA_PEOPLE,
                 group=GOA, created="2026-09-23T19:30")
snacks.update(deletedAt=rel("2026-09-24T10:00"), deletedBy="p-priya")
snacks["history"].append(dict(kind="deleted", at=snacks["deletedAt"], by="p-priya"))
expense("e-goa-lunch", "Beach shack lunch", "food", 4000, "2026-09-24", "p-dev", GOA_PEOPLE,
        group=GOA, created="2026-09-24T14:30")
expense("e-goa-fuel", "Fuel", "travel", 2500, "2026-09-25", "p-dev", GOA_PEOPLE,
        group=GOA, created="2026-09-25T17:30")

# Direct (no group) expenses in September
expense("e-groceries", "Weekend groceries", "food", 2100, "2026-09-19", ME, [ME, "p-priya"],
        created="2026-09-19T13:00")
expense("e-movie", "Movie tickets", "fun", 1600, "2026-09-20", ME, [ME, "p-rohan"],
        due="2026-09-27", created="2026-09-20T22:30")
expense("e-olive", "Dinner at Olive Garden", "food", 2800, "2026-09-30", ME, [ME, "p-priya", "p-esha", "p-dev"],
        due="2026-10-04", created="2026-09-30T19:40")

# College Gang (settled in March)
COLLEGE_PEOPLE = [ME, "p-rohan", "p-priya", "p-esha", "p-dev", "p-kabir"]
expense("e-college-farewell", "Farewell dinner", "food", 9000, "2026-03-12", "p-kabir", COLLEGE_PEOPLE,
        group=COLLEGE, created="2026-03-12T23:00")

# Dubai Weekend (AED, settled in March). rate = ₹ per 1 AED on the expense's date.
DUBAI_PEOPLE = [ME, "p-kabir", "p-meera"]
expense("e-dubai-hotel", "Hotel", "stays", 960, "2026-03-06", "p-kabir", DUBAI_PEOPLE, group=DUBAI,
        currency="AED", rate=dict(value="22.85", to="INR"), created="2026-03-06T22:00")
expense("e-dubai-safari", "Desert safari", "fun", 540, "2026-03-07", ME, DUBAI_PEOPLE, group=DUBAI,
        currency="AED", rate=dict(value="22.80", to="INR"), created="2026-03-07T21:00")
expense("e-dubai-dinner", "Dinner at the Marina", "food", 300, "2026-03-08", "p-meera", DUBAI_PEOPLE, group=DUBAI,
        currency="AED", rate=dict(value="22.90", to="INR"), created="2026-03-08T23:00")

# Flat 302: Rent and Wi-Fi come from the recurring rules, Electricity is added by Meera.
FLAT_PEOPLE = [ME, "p-meera", "p-kabir"]
ELECTRICITY = {4: 1500, 5: 2100, 6: 2400, 7: 1950, 8: 1650, 9: 1350}
for month, bill in ELECTRICITY.items():
    expense(f"e-flat-rent-{month:02}", "Rent", "rent", 36000, f"2026-{month:02}-01", ME, FLAT_PEOPLE,
            group=FLAT, rule="r-rent", created=f"2026-{month:02}-01T09:00")
    expense(f"e-flat-wifi-{month:02}", "Wi-Fi", "bills", 1200, f"2026-{month:02}-05", "p-kabir", FLAT_PEOPLE,
            group=FLAT, rule="r-wifi", created=f"2026-{month:02}-05T09:00")
    expense(f"e-flat-elec-{month:02}", "Electricity bill", "bills", bill, f"2026-{month:02}-26", "p-meera", FLAT_PEOPLE,
            group=FLAT, created=f"2026-{month:02}-26T20:15")

# April–August outings with friends (no group). Together with Flat 302 they give the Insights
# chart its months: Apr ₹18,400 · May ₹21,950 · Jun ₹19,600 · Jul ₹20,600 · Aug ₹22,200.
FOUR = [ME, "p-priya", "p-esha", "p-dev"]
OUTINGS = [
    ("e-apr-canteen", "Dinner at Bombay Canteen", "food", 6000, "2026-04-11", ME, FOUR),
    ("e-apr-ipl", "IPL match tickets", "fun", 8000, "2026-04-18", "p-dev", [ME, "p-dev"]),
    ("e-may-concert", "Concert tickets", "fun", 8000, "2026-05-09", ME, [ME, "p-priya"]),
    ("e-may-lonavala", "Lonavala villa", "stays", 14000, "2026-05-16", ME, FOUR),
    ("e-may-brunch", "Brunch at Farmers’ Café", "food", 2700, "2026-05-24", "p-priya", [ME, "p-priya"]),
    ("e-jun-toit", "Dinner at Toit", "food", 8000, "2026-06-13", ME, FOUR),
    ("e-jun-cricket", "Cricket match tickets", "fun", 8800, "2026-06-21", "p-dev", [ME, "p-dev"]),
    ("e-jul-pizza", "Pizza night", "food", 1200, "2026-07-04", "p-esha", [ME, "p-esha"]),
    ("e-jul-standup", "Stand-up show tickets", "fun", 3600, "2026-07-18", ME, [ME, "p-priya", "p-esha"]),
    ("e-jul-alibaug", "Alibaug stay", "stays", 23000, "2026-07-25", ME, FOUR),
    ("e-aug-bastian", "Dinner at Bastian", "food", 9000, "2026-08-08", ME, FOUR),
    ("e-aug-movie", "Movie night", "fun", 800, "2026-08-15", "p-esha", [ME, "p-esha"]),
    ("e-aug-coorg", "Coorg homestay", "stays", 26400, "2026-08-22", ME, FOUR),
]
for eid, title, category, amount, date, payer, on in OUTINGS:
    expense(eid, title, category, amount, date, payer, on, created=f"{date}T22:00")

# ---------------------------------------------------------------------------------------------
# Payments (all confirmed; pending ones only come from the scenarios)

payments = []


def payment(pid, frm, to, amount, date, time, *, method="upi", group=None, loan=None, exp=None,
            currency="INR", rate=None, recorded_by=None, confirmed_after=15):
    created = dt.datetime.fromisoformat(f"{date}T{time}")
    confirmed = created + dt.timedelta(minutes=confirmed_after)
    payments.append(dict(
        id=pid, fromId=frm, toId=to, amount=rupees(amount), currency=currency, rate=rate,
        method=method, date=rel(date), groupId=group, loanId=loan, expenseId=exp,
        note=None, proof=None, status="confirmed", recordedBy=recorded_by or frm,
        createdAt=rel(created.strftime("%Y-%m-%dT%H:%M")),
        confirmedAt=rel(confirmed.strftime("%Y-%m-%dT%H:%M")), notReceivedNote=None,
    ))


payment("pay-priya-groceries", "p-priya", ME, 1050, "2026-09-29", "18:40", exp="e-groceries", confirmed_after=22)

for index, friend in enumerate(["p-rohan", "p-priya", "p-esha", "p-dev"]):
    payment(f"pay-college-{friend[2:]}", friend, "p-kabir", 1500, "2026-03-14", f"1{index}:00", group=COLLEGE)
payment("pay-college-me", ME, "p-kabir", 1500, "2026-03-14", "14:00", group=COLLEGE)

payment("pay-dubai-me", ME, "p-kabir", 60, "2026-03-14", "18:00", group=DUBAI, currency="AED",
        rate=dict(value="22.90", to="INR"))
payment("pay-dubai-meera", "p-meera", "p-kabir", 300, "2026-03-14", "18:30", group=DUBAI, currency="AED",
        rate=dict(value="22.90", to="INR"))

payment("pay-hackathon-me", ME, "p-esha", 1200, "2026-08-29", "12:00", group=HACKATHON)
payment("pay-hackathon-kabir", "p-kabir", "p-esha", 1400, "2026-08-30", "12:00", group=HACKATHON)

for index, date in enumerate(["2026-07-10", "2026-08-12", "2026-09-14"], start=1):
    payment(f"pay-loan-kabir-{index}", "p-kabir", ME, 1500, date, "11:00", loan="l-kabir-bike")


def month_of(record):
    offset = int(record["date"][1:].split("T")[0])
    return (ANCHOR + dt.timedelta(days=offset)).month


# Monthly outing settlements: each friend squares up that month's outings on the 28th.
for month in range(4, 9):
    owed = {}
    for eid, _, _, amount, date, payer, on in OUTINGS:
        if int(date[5:7]) != month:
            continue
        share = amount / len(on)
        for person in on:
            if person != ME and payer == ME:
                owed[person] = owed.get(person, 0) + share
            elif person == ME and payer != ME:
                owed[payer] = owed.get(payer, 0) - share
    for friend, net in owed.items():
        frm, to = (friend, ME) if net > 0 else (ME, friend)
        payment(f"pay-{month:02}-{friend[2:]}", frm, to, abs(net), f"2026-{month:02}-28", "20:00")


# Flat 302 settles on the 7th of each month with the simplified plan of what's open then
# (that month's rent and Wi-Fi plus last month's electricity). September's electricity stays open.
def simplified(nets, order):
    nets = dict(nets)
    transfers = []
    while True:
        debtors = [p for p in order if nets[p] < 0]
        creditors = [p for p in order if nets[p] > 0]
        if not debtors or not creditors:
            return transfers
        debtor = max(debtors, key=lambda p: (-nets[p], -order.index(p)))
        creditor = max(creditors, key=lambda p: (nets[p], -order.index(p)))
        amount = min(-nets[debtor], nets[creditor])
        transfers.append((debtor, creditor, amount))
        nets[debtor] += amount
        nets[creditor] -= amount


flat_nets = {p: 0 for p in FLAT_PEOPLE}
for month in range(4, 10):
    for e in expenses:
        if e["groupId"] == FLAT and month_of(e) == month and not e["id"].startswith("e-flat-elec"):
            for payer in e["payers"]:
                flat_nets[payer["personId"]] += payer["amount"]
            for row in e["split"]["rows"]:
                flat_nets[row["personId"]] -= row["share"]
    for debtor, creditor, amount in simplified(flat_nets, FLAT_PEOPLE):
        payment(f"pay-flat-{month:02}-{debtor[2:] if debtor != ME else 'me'}", debtor, creditor,
                amount / 100, f"2026-{month:02}-07", "19:00", group=FLAT)
        flat_nets[debtor] += amount
        flat_nets[creditor] -= amount
    electricity = next(e for e in expenses if e["id"] == f"e-flat-elec-{month:02}")
    flat_nets["p-meera"] += electricity["amount"]
    for row in electricity["split"]["rows"]:
        flat_nets[row["personId"]] -= row["share"]
assert flat_nets == {ME: -45000, "p-meera": 90000, "p-kabir": -45000}, flat_nets

# ---------------------------------------------------------------------------------------------
# Project components

components = []


def component(cid, project, name, status, estimate, actual, payer, changed, created=None):
    components.append(dict(
        id=cid, projectId=project, name=name, status=status,
        estimatedCost=rupees(estimate) if estimate is not None else None,
        actualCost=rupees(actual) if actual is not None else None,
        paidBy=payer, receipt=None, createdAt=rel(created or f"{changed}T10:00"),
        statusChangedAt=rel(f"{changed}T18:00"),
        history=[dict(kind="added", at=rel(created or f"{changed}T10:00"), by=payer)]
        + ([] if status == "planned" else [dict(kind=status, at=rel(f"{changed}T18:00"), by=payer)]),
    ))


component("c-drone-gps", DRONE, "GPS module", "planned", 6000, None, ME, "2026-09-20")
component("c-drone-camera", DRONE, "Camera", "bought", None, 7500, "p-dev", "2026-09-18")
component("c-drone-transmitter", DRONE, "Transmitter", "bought", 5000, 5000, ME, "2026-09-09", "2026-08-10T12:00")
component("c-drone-escs", DRONE, "ESCs and propellers", "bought", 8000, 8000, ME, "2026-09-02", "2026-08-10T12:00")
component("c-drone-battery", DRONE, "Battery", "bought", 5000, 4500, "p-rohan", "2026-08-27", "2026-08-10T12:00")
component("c-drone-fc", DRONE, "Flight controller", "bought", 9000, 9000, "p-priya", "2026-08-21", "2026-08-10T12:00")
component("c-drone-motors", DRONE, "Motors ×4", "done", 12000, 12000, "p-dev", "2026-08-16", "2026-08-10T12:00")
component("c-drone-frame", DRONE, "Frame", "done", 6000, 6000, "p-dev", "2026-08-12", "2026-08-10T12:00")

component("c-hack-pi", HACKATHON, "Raspberry Pi kits", "done", 7000, 7200, "p-esha", "2026-08-05", "2026-08-01T11:00")
component("c-hack-sensors", HACKATHON, "Sensors", "done", 5000, 4600, "p-dev", "2026-08-08", "2026-08-01T11:00")
component("c-hack-display", HACKATHON, "Display", "done", 3500, 3400, ME, "2026-08-12", "2026-08-01T11:00")
component("c-hack-cables", HACKATHON, "Cables and adapters", "done", 3000, 3200, "p-kabir", "2026-08-15", "2026-08-01T11:00")

# ---------------------------------------------------------------------------------------------
# Loans, recurring rules, drafts

loans = [dict(
    id="l-kabir-bike", lenderId=ME, borrowerId="p-kabir", amount=rupees(4500), currency="INR", rate=None,
    reason="Bike service", date=rel("2026-06-12"),
    installments=dict(count=3, frequency="monthly", firstDue=rel("2026-07-12")), dueDate=None,
    createdAt=rel("2026-06-12T19:00"), createdBy=ME,
)]

recurring_rules = [
    dict(id="r-rent", groupId=FLAT, title="Rent", category="rent", amount=rupees(36000), currency="INR",
         variable=False, frequency="monthly", anchorDate=rel("2026-09-01"), startDate=rel("2026-04-01"),
         lastOccurrence=rel("2026-09-01"), payerId=ME, split=dict(mode="equal", personIds=FLAT_PEOPLE),
         createdAt=rel("2026-03-28T19:10"), createdBy=ME, active=True),
    dict(id="r-wifi", groupId=FLAT, title="Wi-Fi", category="bills", amount=rupees(1200), currency="INR",
         variable=False, frequency="monthly", anchorDate=rel("2026-09-05"), startDate=rel("2026-04-05"),
         lastOccurrence=rel("2026-09-05"), payerId="p-kabir", split=dict(mode="equal", personIds=FLAT_PEOPLE),
         createdAt=rel("2026-03-28T19:15"), createdBy="p-kabir", active=True),
    dict(id="r-gas", groupId=FLAT, title="Cooking gas", category="bills", amount=None, currency="INR",
         variable=True, frequency="monthly", anchorDate=rel("2026-09-28"), startDate=rel("2026-09-28"),
         lastOccurrence=rel("2026-09-28"), payerId=ME, split=dict(mode="equal", personIds=FLAT_PEOPLE),
         createdAt=rel("2026-09-10T21:00"), createdBy=ME, active=True),
]

drafts = [dict(id="d-gas-09", ruleId="r-gas", occurrenceDate=rel("2026-09-28"),
               createdAt=rel("2026-09-28T09:00"), expenseId=None)]

# ---------------------------------------------------------------------------------------------
# Reminder log (automatic reminders Paybak sent for Arjun before the scheduler cursor)

reminders = []
for index, when in enumerate(["2026-08-10", "2026-09-10", "2026-09-12"], start=1):
    installment = 2 if when == "2026-08-10" else 3
    reminders.append(dict(id=f"rem-kabir-loan-{index}", toId="p-kabir", fromId=ME, amount=rupees(1500),
                          currency="INR", expenseId=None, groupId=None, loanId="l-kabir-bike",
                          installment=installment, sentAt=rel(f"{when}T21:00"), automatic=True, message=None))
for index, when in enumerate(["2026-09-25", "2026-09-27"], start=1):
    reminders.append(dict(id=f"rem-rohan-{index}", toId="p-rohan", fromId=ME, amount=rupees(800),
                          currency="INR", expenseId="e-movie", groupId=None, loanId=None, installment=None,
                          sentAt=rel(f"{when}T21:00"), automatic=True, message=None))

# ---------------------------------------------------------------------------------------------
# Inbox (things that happened to Arjun before the scheduler cursor; all read)

notifications = []


def notify(nid, kind, at, **params):
    notifications.append(dict(id=nid, type=kind, createdAt=rel(at), read=True, params=params))


for eid, at in [("e-goa-scooter", "2026-09-21T11:00"), ("e-goa-villa", "2026-09-21T15:30"),
                ("e-goa-parasailing", "2026-09-23T17:00"), ("e-goa-lunch", "2026-09-24T14:30"),
                ("e-goa-fuel", "2026-09-25T17:30"), ("e-flat-elec-09", "2026-09-26T20:15")]:
    e = next(x for x in expenses if x["id"] == eid)
    share = next(r["share"] for r in e["split"]["rows"] if r["personId"] == ME)
    total = 1750000 if eid == "e-goa-villa" else e["amount"]  # Villa was ₹17,500 when it was added
    notify(f"n-new-{eid[2:]}", "newExpenseInGroup", at, expenseId=eid, groupId=e["groupId"],
           actorId=e["createdBy"], title=e["title"], total=total, share=total // 5 if eid == "e-goa-villa" else share,
           currency="INR")
notify("n-overdue-rohan-movie", "paymentOverdue", "2026-09-28T09:00", personId="p-rohan", amount=rupees(800),
       currency="INR", title="Movie tickets", expenseId="e-movie", groupId=None, loanId=None,
       dueDate=rel("2026-09-27"))
notify("n-confirmed-priya", "paymentConfirmed", "2026-09-29T19:02", paymentId="pay-priya-groceries",
       personId="p-priya", amount=rupees(1050), currency="INR", title="Weekend groceries", method="upi")

# ---------------------------------------------------------------------------------------------
# Scenarios: store actions applied on top of the base state (domain.md §7.4)

scenarios = {
    "empty": [dict(action="clearLedger")],
    "eshaClaimsPayment": [dict(action="recordPayment", at="D0T21:12", payment=dict(
        id="pay-esha-olive", fromId="p-esha", toId=ME, amount=rupees(700), currency="INR", method="upi",
        date="D0", groupId=None, loanId=None, expenseId="e-olive", recordedBy="p-esha"))],
    "eshaPaymentConfirmed": [dict(use="eshaClaimsPayment"),
                             dict(action="confirmPayment", at="D0T21:15", paymentId="pay-esha-olive")],
    "eshaPaymentNotReceived": [dict(use="eshaClaimsPayment"),
                               dict(action="markNotReceived", at="D0T21:15", paymentId="pay-esha-olive",
                                    note="Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check your UPI app?")],
    "paymentToMeeraPending": [dict(action="recordPayment", at="D0T21:14", payment=dict(
        id="pay-me-meera", fromId=ME, toId="p-meera", amount=rupees(450), currency="INR", method="cash",
        date="D0", groupId=FLAT, loanId=None, expenseId=None, recordedBy=ME))],
    "paymentToMeeraConfirmed": [dict(use="paymentToMeeraPending"),
                                dict(action="confirmPayment", at="D0T21:15", paymentId="pay-me-meera")],
    "paymentToKabirPending": [dict(action="recordPayment", at="D0T21:14", payment=dict(
        id="pay-me-kabir", fromId=ME, toId="p-kabir", amount=rupees(1400), currency="INR", method="upi",
        date="D0", groupId=GOA, loanId=None, expenseId=None, recordedBy=ME))],
    "eshaFlagsSeafood": [dict(action="flagExpense", at="D0T20:30", expenseId="e-goa-seafood", by="p-esha",
                              note="I left before dessert. Can we check the bill?")],
    "lendDev": [dict(action="addLoan", at="D0T21:14", loan=dict(
        id="l-dev-laptop", lenderId=ME, borrowerId="p-dev", amount=rupees(6000), currency="INR",
        reason="Laptop repair", date="D0", installments=dict(count=3, frequency="monthly", firstDue="D+30"),
        dueDate=None))],
    "lendDevOverdue": [dict(use="lendDev"), dict(action="setClock", at="D+34T10:00")],
    "devBuysGps": [dict(action="updateComponent", at="D0T21:00", componentId="c-drone-gps",
                        status="bought", actualCost=rupees(9500), paidBy="p-dev")],
    "closeDrone": [dict(action="closeProject", at="D0T21:10", projectId=DRONE)],
    "weekendTrek": [dict(action="createGroup", at="D0T21:14", group=dict(
        id="g-trek", kind="group", type="trip", icon="plane", name="Weekend Trek", currency="INR",
        memberIds=[ME, "p-esha", "p-dev", "p-kabir"], simplifyDebts=True, settleBy=None))],
    "allSettled": [
        dict(action="recordPayment", at="D0T21:13", payment=dict(id="pay-settle-kabir", fromId=ME, toId="p-kabir",
             amount=rupees(1400), currency="INR", method="upi", date="D0", groupId=GOA, loanId=None, expenseId=None, recordedBy=ME)),
        dict(action="recordPayment", at="D0T21:13", payment=dict(id="pay-settle-meera", fromId=ME, toId="p-meera",
             amount=rupees(450), currency="INR", method="upi", date="D0", groupId=FLAT, loanId=None, expenseId=None, recordedBy=ME)),
        dict(action="recordPayment", at="D0T21:13", payment=dict(id="pay-settle-rohan", fromId="p-rohan", toId=ME,
             amount=rupees(800), currency="INR", method="upi", date="D0", groupId=None, loanId=None, expenseId="e-movie", recordedBy="p-rohan")),
        *[dict(action="recordPayment", at="D0T21:13", payment=dict(id=f"pay-settle-{p[2:]}", fromId=p, toId=ME,
               amount=rupees(700), currency="INR", method="upi", date="D0", groupId=None, loanId=None, expenseId="e-olive", recordedBy=p))
          for p in ["p-priya", "p-esha", "p-dev"]],
        *[dict(action="confirmPayment", at="D0T21:14", paymentId=pid) for pid in
          ["pay-settle-kabir", "pay-settle-meera", "pay-settle-rohan", "pay-settle-priya", "pay-settle-esha", "pay-settle-dev"]],
    ],
    "pro": [dict(action="setEntitlement", plan="pro", period="yearly", trialEndsAt="D+7")],
}

# ---------------------------------------------------------------------------------------------

demo = dict(
    schemaVersion=1,
    anchor=dict(figmaDate="2026-09-30", pinnedTime="21:15",
                note="D±n = the load day ± n days (local). Pin the clock to D0T21:15 to match Figma."),
    profile=dict(name="Arjun Mehta", avatar=dict(kind="preset", index=0), currencyCode="INR",
                 upiID="arjun@okaxis", username="arjun", pronoun="he", signInMethod="email",
                 contact="arjun@example.com", onboardingComplete=True, showPaymentToFriends=True,
                 paymentMethods=[dict(id="pm-upi", kind="upi", value="arjun@okaxis", primary=True),
                                 dict(id="pm-hdfc", kind="bank", bankName="HDFC Bank", last4="4821", primary=False)]),
    settings=dict(
        keepBalancesPerCurrency=False,
        push=dict(addedToExpense=True, paymentsToConfirm=True, reminders=True, overdueAlerts=True,
                  projectUpdates=True, monthlySummary=True),
        reminderSchedule=dict(twoDaysBefore=True, onDueDate=True, overdueEvery3Days=True, time="21:00"),
        discovery=dict(findMeByContact=True, contactsSync=True),
        entitlement=dict(plan="free", period=None, trialEndsAt=None, since=None),
    ),
    people=people, groups=groups, expenses=expenses, payments=payments, loans=loans,
    components=components, recurringRules=recurring_rules, drafts=drafts, reminders=reminders,
    inbox=notifications, rotation={}, scheduler=dict(cursor="D0T00:00"),
    scenarios=scenarios,
)

out = Path(__file__).with_name("demo.json")
out.write_text(json.dumps(demo, ensure_ascii=False, indent=1) + "\n")
print(f"wrote {out} ({len(expenses)} expenses, {len(payments)} payments, {len(components)} components)")

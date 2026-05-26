# TODO - Autopay Admin/Employee Enhancements

## Step 1: Update employee data model
- [ ] Add `flagged` column to `data/employees.csv` (default existing rows to `0`)
- [ ] Update `src/Employee.java` to include `boolean flagged`

## Step 2: Admin dashboard control panel + UI
- [ ] Update `src/AdminDashboard.java` layout to include right-side control panel
- [ ] When clicking an employee row, populate:
  - [ ] Salary edit input + Update button
  - [ ] Flagged checkbox
- [ ] Implement row highlighting light red for flagged employees

## Step 3: Table features (sortable + search/filter)
- [ ] Make employee table sortable by column header
- [ ] Add search bar (name/id/position)
- [ ] Add dropdown filter: All / Flagged Only

## Step 4: Automation buttons (presentation)
- [ ] Add `Start Payroll` automation button processing current month
- [ ] Flagged employees must remain unprocessed/red
- [ ] Add `Redo Payroll` button to reset payroll statuses and re-run automation

## Step 5: Create employee => auto-create login account + payroll record
- [ ] On `Add Employee` in admin:
  - [ ] Create user account in `data/users.csv` using username `emp<ID>`
  - [ ] Default password `emp123`
  - [ ] Ensure payroll record exists for current month and shows in payroll tab

## Step 6: Employee dashboard prototype presentation features
- [ ] Update `src/EmployeeDashboard.java`:
  - [ ] Show flagged message when flagged
  - [ ] Add Change Password UI (updates CSV; for prototype only)
  - [ ] Add payment method UI prototype (doesn’t need to work)

## Step 7: Verify
- [ ] Run app and verify all behaviors manually


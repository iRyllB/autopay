# TODO - Auto employee login generation

- [x] Update EmployeeDashboard to find employee by username == employee name
- [x] Update AdminDashboard createEmployeeUserAccount to use:
  - username = employee name
  - password = employee name + "123"
- [x] Add AdminDashboard logic to regenerate `data/users.csv` from `data/employees.csv` on Admin open, preserving Admin account(s)
- [ ] Regenerate `data/users.csv` contents in the repo to match `data/employees.csv` (so login works immediately)
- [x] Compile/run and verify app starts (login system no longer crashes)
- [ ] Verify employee login with a specific sample from employees.csv (e.g., John Doe / John Doe123)


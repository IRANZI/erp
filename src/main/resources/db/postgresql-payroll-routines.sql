create or replace function create_payslip_payment_message()
returns trigger
language plpgsql
as $$
declare
    payment_message text;
begin
    if new.status = 'PAID' and (tg_op = 'INSERT' or old.status is distinct from new.status) then
        select format(
            'Dear %s Your salary of %s/%s from %s %s has been credited to your %s account Successfully.',
            e.first_name,
            lpad(new.salary_month::text, 2, '0'),
            new.salary_year,
            emp.institution,
            trim(to_char(new.net_salary, 'FM999999999999999990.00')),
            e.employee_code
        )
        into payment_message
        from employees e
        join employment emp on emp.employee_id = e.id
        where e.id = new.employee_id;

        insert into messages (employee_id, payslip_id, salary_month, salary_year, content, created_at)
        values (new.employee_id, new.id, new.salary_month, new.salary_year, payment_message, now());
    end if;

    return new;
end;
$$;;

drop trigger if exists trg_payslip_payment_message on payslips;;

create trigger trg_payslip_payment_message
after insert or update of status on payslips
for each row
execute function create_payslip_payment_message();;

create or replace function approve_payroll(p_month integer, p_year integer)
returns integer
language plpgsql
as $$
declare
    paid_count integer;
begin
    update payslips
    set status = 'PAID',
        paid_at = coalesce(paid_at, now())
    where salary_month = p_month
      and salary_year = p_year
      and status = 'PENDING';

    get diagnostics paid_count = row_count;
    return paid_count;
end;
$$;;

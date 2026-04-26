type PageHeaderProps = {
  title: string;
  subtitle?: string;
  className?: string;
};

export function PageHeader({ title, subtitle, className = "" }: PageHeaderProps) {
  return (
    <header className={`mb-[34px] text-right ${className}`}>
      <h1 className="mb-2.5 text-[27px] font-bold leading-tight text-primary">{title}</h1>
      {subtitle ? <p className="text-[10.5px] leading-relaxed text-on-surface-variant/75">{subtitle}</p> : null}
    </header>
  );
}

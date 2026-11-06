
import logoImg from '../assets/logo_cropped.png';

export function Logo({ className = '' }: { className?: string }) {
  return (
    <div className={`brand-logo ${className}`} style={{ display: 'flex', alignItems: 'center', textDecoration: 'none' }}>
      <img
        src={logoImg}
        alt="Assure Technologies"
        className="brand-logo-img"
        style={{
          height: '42px',
          width: 'auto',
          maxWidth: '220px',
          objectFit: 'contain',
          display: 'block',
          backgroundColor: '#ffffff',
          padding: '2px 8px',
          borderRadius: '6px'
        }}
      />
    </div>
  );
}

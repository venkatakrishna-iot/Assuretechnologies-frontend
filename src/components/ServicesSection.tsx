import { useServices } from '../hooks/useServices';
import './ServicesSection.css';

export function ServicesSection() {
  const { services, loading } = useServices();

  if (loading) {
    return (
      <section className="services-section">
        <div className="services-header">
          <h2>Services We Offer</h2>
          <div className="services-divider"></div>
          <p>Loading services...</p>
        </div>
      </section>
    );
  }

  return (
    <section className="services-section">
      <div className="services-header">
        <h2>Services We Offer</h2>
        <div className="services-divider"></div>
        <p>Comprehensive technology and infrastructure solutions for your business</p>
      </div>

      <div className="services-grid">
        {(Array.isArray(services) ? services : []).map(service => (
          <div key={service.id} className="service-card">
            <div className="service-icon image-icon">
              {service.image
                ? <img src={service.image} alt={service.name} />
                : <div className="service-placeholder" />}
            </div>
            <h5 className="service-label">{(typeof service.category === 'object' && service.category !== null ? service.category.name : service.category) || ''}</h5>
            <p className="service-title">{service.name}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

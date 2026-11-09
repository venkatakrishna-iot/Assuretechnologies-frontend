import { BASE_URL } from '../services/api';
import type { Product } from '../data/products';

/**
 * Normalizes raw backend product data into consistent frontend Product representation.
 */
export const mapRawProduct = (p: any): Product => {
  const base = parseFloat(p.base_price) || 0;
  const disc = parseFloat(p.discount) || 0;
  const comm = parseFloat(p.admin_commission) || 0;
  const slab = p.tax_slab != null ? parseFloat(p.tax_slab) : 18;

  const bannerImg = p.banner || (Array.isArray(p.images) && p.images.length > 0 ? p.images[0] : null);
  const img = bannerImg
    ? (bannerImg.startsWith('http') || bannerImg.startsWith('blob:')
      ? bannerImg
      : `${BASE_URL}${bannerImg.startsWith('/') ? '' : '/'}${bannerImg}`)
    : 'https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=400&h=400&fit=crop';

  const catName = typeof p.category === 'object' && p.category !== null && p.category.name
    ? p.category.name
    : (typeof p.category === 'string' ? p.category : (p.service || 'General'));

  const adminCommission = base * (comm / 100);
  const vendorDiscountedPrice = base * (1 - (disc / 100));
  const sellingPriceExclTax = vendorDiscountedPrice + adminCommission;
  const originalPriceExclTax = base + adminCommission;

  const taxAmount = sellingPriceExclTax * (slab / 100);
  const originalTaxAmount = originalPriceExclTax * (slab / 100);

  const finalPrice = parseFloat((sellingPriceExclTax + taxAmount).toFixed(2));
  const originalPrice = parseFloat((originalPriceExclTax + originalTaxAmount).toFixed(2));

  return {
    id: p.id || p.product_id,
    name: p.name,
    service: catName,
    price: finalPrice,
    originalPrice,
    discount: disc,
    tax_slab: slab,
    description: p.description || '',
    image: img,
    rating: Number(p.rating) || 4.5,
    reviewCount: Number(p.reviewCount) || 20,
    inStock: p.stock > 0
  };
};

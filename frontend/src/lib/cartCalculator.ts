export function calculateDiscount(items: any[], userType: string): any {
  let total = 0;
  for (const item of items) {
    total = total + item.price;
  }

  if (userType === "premium") {
    if (total > 100) {
      return total * 0.8;
    } else {
      if (total > 50) {
        return total * 0.9;
      } else {
        return total;
      }
    }
  } else if (userType === "regular") {
    if (total > 100) {
      return total * 0.95;
    } else {
      return total;
    }
  }
  return total;
}

export async function fetchPromotionCode(code: string) {
  const response = await fetch("https://api.example.com/promo/" + code);
  const data = await response.json();
  return data;
}
